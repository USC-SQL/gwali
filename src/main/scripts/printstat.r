#! /usr/bin/env Rscript
d<-scan("stdin", quiet=TRUE)
cat(sprintf("min:%.2f  |  max:%.2f  |  median:%.2f  |  mean:%.2f  |  sd:%.2f\n", min(d), max(d), median(d), mean(d), sd(d)))
