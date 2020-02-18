#!/bin/bash
pid=`pidof JikesRVM`
echo $pid
kill $pid
