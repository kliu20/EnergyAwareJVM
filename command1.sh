sudo dist/FullAdaptiveMarkSweep_x86_64-linux/rvm  "-Xmx2500M" "-X:vm:errorsFatal=true" "-X:aos:enable_recompilation=true" "-X:aos:hot_method_time_min=20" "-X:aos:hot_method_time_max=800" "-X:aos:frequency_to_be_printed=260000" "-X:aos:event_counter=cache-misses" "-X:aos:enable_counter_profiling=false" "-X:aos:enable_energy_profiling=true" "-X:aos:profiler_file=out.csv" "-X:aos:enable_scaling_by_counters=false" "-X:aos:enable_counter_printer=true" "-jar" "dacapo-9.12-bach.jar" "-s" "large" "sunflow"