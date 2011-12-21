/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.mmtk.plan.generational;

import org.mmtk.plan.*;
import org.mmtk.policy.CopySpace;
import org.mmtk.policy.Space;

import org.mmtk.utility.deque.*;
import org.mmtk.utility.heap.Map;
import org.mmtk.utility.heap.VMRequest;
import org.mmtk.utility.Log;
import org.mmtk.utility.options.Options;
import org.mmtk.utility.sanitychecker.SanityChecker;
import org.mmtk.utility.statistics.*;

import org.mmtk.vm.VM;

import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

/**
 * This abstract class implements the core functionality of generic
 * two-generationa copying collectors.  Nursery collections occur when
 * either the heap is full or the nursery is full.  The nursery size
 * is determined by an optional command line argument.  If undefined,
 * the nursery size is "infinite", so nursery collections only occur
 * when the heap is full (this is known as a flexible-sized nursery
 * collector).  Thus both fixed and flexible nursery sizes are
 * supported.  Full heap collections occur when the nursery size has
 * dropped to a statically defined threshold,
 * <code>NURSERY_THRESHOLD</code><p>
 *
 * See also Plan.java for general comments on local vs global plan
 * classes.
 */
@Uninterruptible
public abstract class Gen extends StopTheWorld {

  /*****************************************************************************
   *
   * Constants
   */
  public static final float DEFAULT_PRETENURE_THRESHOLD_FRACTION = 0.5f; // if object is bigger than this fraction of nursery, pretenure to LOS
  protected static final float SURVIVAL_ESTIMATE = 0.8f; // est yield
  protected static final float MATURE_FRACTION = 0.5f; // est yield
  private static final float WORST_CASE_COPY_EXPANSION = 1.5f; // worst case for addition of one word overhead due to address based hashing
  public static final boolean IGNORE_REMSETS = false;
  public static final boolean USE_NON_HEAP_OBJECT_REFERENCE_WRITE_BARRIER = false;
  public static final boolean USE_OBJECT_BARRIER_FOR_AASTORE = false; // choose between slot and object barriers
  public static final boolean USE_OBJECT_BARRIER_FOR_PUTFIELD = false; // choose between slot and object barriers
  public static final boolean USE_OBJECT_BARRIER = USE_OBJECT_BARRIER_FOR_AASTORE || USE_OBJECT_BARRIER_FOR_PUTFIELD;

  /** Fraction of available virtual memory to give to the nursery (if contiguous) */
  protected static final float NURSERY_VM_FRACTION = 0.15f;

  /** Switch between a contiguous and discontiguous nursery (experimental) */
  static final boolean USE_DISCONTIGUOUS_NURSERY = false;

  // Allocators
  public static final int ALLOC_NURSERY        = ALLOC_DEFAULT;
  public static final int ALLOC_MATURE         = StopTheWorld.ALLOCATORS + 1;
  public static final int ALLOC_MATURE_MINORGC = StopTheWorld.ALLOCATORS + 2;
  public static final int ALLOC_MATURE_MAJORGC = StopTheWorld.ALLOCATORS + 3;

  public static final int SCAN_NURSERY = 0;
  public static final int SCAN_MATURE  = 1;

  /*****************************************************************************
   *
   * Class fields
   */

  /* Statistics */
  protected static final BooleanCounter fullHeap = new BooleanCounter("majorGC", true, true);
  private static final Timer fullHeapTime = new Timer("majorGCTime", false, true);
  protected static final EventCounter wbFast;
  protected static final EventCounter wbSlow;
  public static final SizeCounter nurseryMark;
  public static final SizeCounter nurseryCons;

  /** The nursery space is where all new objects are allocated by default */
  private static final VMRequest vmRequest = USE_DISCONTIGUOUS_NURSERY ? VMRequest.create() : VMRequest.create(NURSERY_VM_FRACTION, true);
  public static final CopySpace nurserySpace = new CopySpace("nursery", false, vmRequest);

  public static final int NURSERY = nurserySpace.getDescriptor();
  private static final Address NURSERY_START = nurserySpace.getStart();

  /*****************************************************************************
   *
   * Instance fields
   */
  /* status fields */
  public boolean gcFullHeap = false;
  public boolean nextGCFullHeap = false;

  /* The trace object */
  public final Trace nurseryTrace = new Trace(metaDataSpace);

  /**
   * Remset pools
   */
  public final SharedDeque modbufPool = new SharedDeque("modBufs",metaDataSpace, 1);
  public final SharedDeque remsetPool = new SharedDeque("remSets",metaDataSpace, 1);
  public final SharedDeque arrayRemsetPool = new SharedDeque("arrayRemSets",metaDataSpace, 2);

  /*
   * Class initializer
   */
  static {
    if (GATHER_WRITE_BARRIER_STATS) {
      wbFast = new EventCounter("wbFast");
      wbSlow = new EventCounter("wbSlow");
    } else {
      wbFast = null;
      wbSlow = null;
    }
    if (Stats.GATHER_MARK_CONS_STATS) {
      nurseryMark = new SizeCounter("nurseryMark", true, true);
      nurseryCons = new SizeCounter("nurseryCons", true, true);
    } else {
      nurseryMark = null;
      nurseryCons = null;
    }
  }

  /*****************************************************************************
   *
   * Collection
   */

  /**
   * Force the next collection to be full heap.
   */
  public void forceFullHeapCollection() {
    nextGCFullHeap = true;
  }

  /**
   * Perform a (global) collection phase.
   *
   * @param phaseId Collection phase to execute.
   */
  @NoInline
  public void collectionPhase(short phaseId) {
    if (phaseId == SET_COLLECTION_KIND) {
      super.collectionPhase(phaseId);
      gcFullHeap = requiresFullHeapCollection();
      return;
    }

    if (phaseId == PREPARE) {
      nurserySpace.prepare(true);
      if (traceFullHeap()){
        if (gcFullHeap) {
          if (Stats.gatheringStats()) fullHeap.set();
          fullHeapTime.start();
        }
        super.collectionPhase(phaseId);

        // we can throw away the remsets (but not modbuf) for a full heap GC
        remsetPool.clearDeque(1);
        arrayRemsetPool.clearDeque(2);
      }
      return;
    }

    if (phaseId == CLOSURE) {
      if (!traceFullHeap()) {
        nurseryTrace.prepare();
      }
      return;
    }

    if (phaseId == RELEASE) {
      nurserySpace.release();
      modbufPool.clearDeque(1);
      remsetPool.clearDeque(1);
      arrayRemsetPool.clearDeque(2);
      if (!traceFullHeap()) {
        nurseryTrace.release();
      } else {
        super.collectionPhase(phaseId);
        if (gcFullHeap) fullHeapTime.stop();
      }
      nextGCFullHeap = (getPagesAvail() < Options.nurserySize.getMinNursery());
      return;
    }

    super.collectionPhase(phaseId);
  }

  /**
   * This method controls the triggering of a GC. It is called periodically
   * during allocation. Returns true to trigger a collection.
   *
   * @param spaceFull Space request failed, must recover pages within 'space'.
   * @return True if a collection is requested by the plan.
   */
  public final boolean collectionRequired(boolean spaceFull, Space space) {
    int availableNurseryPages = Options.nurserySize.getMaxNursery() - nurserySpace.reservedPages();

    /* periodically recalculate nursery pretenure threshold */
    Plan.pretenureThreshold = (int) ((availableNurseryPages<<LOG_BYTES_IN_PAGE) * Options.pretenureThresholdFraction.getValue());

    if (availableNurseryPages <= 0) {
      return true;
    }

    if (virtualMemoryExhausted()) {
      return true;
    }

    if (spaceFull && space != nurserySpace) {
      nextGCFullHeap = true;
    }

    return super.collectionRequired(spaceFull, space);
  }

  /**
   * Determine if this GC should be a full heap collection.
   *
   * @return True is this GC should be a full heap collection.
   */
  protected boolean requiresFullHeapCollection() {
    if (userTriggeredCollection && Options.fullHeapSystemGC.getValue()) {
      return true;
    }

    if (nextGCFullHeap || collectionAttempt > 1) {
      // Forces full heap collection
      return true;
    }

    if (virtualMemoryExhausted()) {
      return true;
    }

    return false;
  }

  /**
   * Independent of how many pages remain in the page budget (a function of
   * heap size), we must ensure we never exhaust virtual memory.  Therefore
   * we must never let the nursery grow to the extent that it can't be
   * copied into the mature space.
   *
   * @return True if the nursery has grown to the extent that it may not be
   * able to be copied into the mature space.
   */
  private boolean virtualMemoryExhausted() {
    return ((int)(getCollectionReserve() * WORST_CASE_COPY_EXPANSION)) >= getMaturePhysicalPagesAvail();
  }

  /*****************************************************************************
   *
   * Correctness
   */

  /*****************************************************************************
   *
   * Accounting
   */

  /**
   * Return the number of pages in use given the pending
   * allocation.  Simply add the nursery's contribution to that of
   * the superclass.
   *
   * @return The number of pages reserved given the pending
   * allocation, excluding space reserved for copying.
   */
  @Override
  public int getPagesUsed() {
    return (nurserySpace.reservedPages() + super.getPagesUsed());
  }

  /**
   * Return the number of pages available for allocation, <i>assuming
   * all future allocation is to the nursery</i>.
   *
   * @return The number of pages available for allocation, <i>assuming
   * all future allocation is to the nursery</i>.
   */
  @Override
  public int getPagesAvail() {
    return super.getPagesAvail() >> 1;
  }

  /**
   * Return the number of pages reserved for copying.
   *
   * @return The number of pages reserved given the pending
   * allocation, including space reserved for copying.
   */
  @Override
  public int getCollectionReserve() {
    return nurserySpace.reservedPages() + super.getCollectionReserve();
  }

  /**
   * Return the number of pages available for allocation into the mature
   * space.
   *
   * @return The number of pages available for allocation into the mature
   * space.
   */
  public abstract int getMaturePhysicalPagesAvail();

  /*****************************************************************************
   *
   * Miscellaneous
   */

  /**
   * Return true if the address resides within the nursery
   *
   * @param addr The object to be tested
   * @return true if the address resides within the nursery
   */
  @Inline
  static boolean inNursery(Address addr) {
    if (USE_DISCONTIGUOUS_NURSERY)
      return Map.getDescriptorForAddress(addr) == NURSERY;
    else
      return addr.GE(NURSERY_START);
  }

  /**
   * Return true if the object resides within the nursery
   *
   * @param obj The object to be tested
   * @return true if the object resides within the nursery
   */
  @Inline
  static boolean inNursery(ObjectReference obj) {
    return inNursery(obj.toAddress());
  }

  /**
   * @return Does the mature space do copying ?
   */
  protected boolean copyMature() {
    return false;
  }

  /**
   * Print pre-collection statistics. In this class we prefix the output
   * indicating whether the collection was full heap or not.
   */
  public void printPreStats() {
    if ((Options.verbose.getValue() >= 1) && (gcFullHeap))
      Log.write("[Full heap]");
    super.printPreStats();
  }

  /**
   * @return The mature space, set by each subclass of <code>Gen</code>.
   */
  protected abstract Space activeMatureSpace();

  /**
   * @return True if we should trace the whole heap during collection. True if
   *         we're ignorning remsets or if we're doing a full heap GC.
   */
  public final boolean traceFullHeap() {
    return IGNORE_REMSETS || gcFullHeap;
  }

  /**
   * @return Is current GC only collecting objects allocated since last GC.
   */
  public final boolean isCurrentGCNursery() {
    return !gcFullHeap;
  }

  /**
   * @return Is last GC a full collection?
   */
  public final boolean lastCollectionFullHeap() {
    return gcFullHeap;
  }

  /**
   * @see org.mmtk.plan.Plan#willNeverMove
   *
   * @param object Object in question
   * @return True if the object will never move
   */
  @Override
  public boolean willNeverMove(ObjectReference object) {
    if (Space.isInSpace(NURSERY, object))
      return false;
    return super.willNeverMove(object);
  }

  /**
   * Return the expected reference count. For non-reference counting
   * collectors this becomes a true/false relationship.
   *
   * @param object The object to check.
   * @param sanityRootRC The number of root references to the object.
   * @return The expected (root excluded) reference count.
   */
  public int sanityExpectedRC(ObjectReference object, int sanityRootRC) {
    Space space = Space.getSpaceForObject(object);

    // Nursery
    if (space == Gen.nurserySpace) {
      return SanityChecker.DEAD;
    }

    // Immortal spaces
    if (space == Gen.immortalSpace || space == Gen.vmSpace) {
      return space.isReachable(object) ? SanityChecker.ALIVE : SanityChecker.DEAD;
    }

    // Mature space (nursery collection)
    if (VM.activePlan.global().isCurrentGCNursery()) {
      return SanityChecker.UNSURE;
    }

    // Mature space (full heap collection)
    return space.isReachable(object) ? SanityChecker.ALIVE : SanityChecker.DEAD;
  }

  /**
   * Register specialized methods.
   */
  @Interruptible
  protected void registerSpecializedMethods() {
    TransitiveClosure.registerSpecializedScan(SCAN_NURSERY, GenNurseryTraceLocal.class);
    super.registerSpecializedMethods();
  }
}
