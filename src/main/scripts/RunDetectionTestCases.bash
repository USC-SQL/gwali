#! /bin/bash
if [ $# -ne 1 ]
then
 echo "usage: RunDetectionTestCases Test_Cases_File"
 exit;
fi
jarlocation="/home/alameer/workspace/gwali/target/gwali-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
logfolder="log"
declare -a ARRAY
filename=${1%}
linecount=0
tempfolder="$logfolder/Issues"

rm -r $logfolder
mkdir $logfolder

while read -r line
do
    ARRAY[$linecount]=$line
    ((linecount++))
done < "$filename"
c=2
basepath="file://"${ARRAY[1]}
totalcases=${ARRAY[0]}

truepos=0
trueneg=0
falsepos=0
falseneg=0
failure=0
while [ $c -lt $((linecount)) ]
do
    output="unknown"
    baselinepage=$basepath""${ARRAY[c]}
    pageundertest=$basepath""${ARRAY[c+1]}
    expectedoutput=${ARRAY[c+2]}
    (time java -jar $jarlocation -b $baselinepage -t $pageundertest -d $logfolder/DetectOutput$c.txt -l $logfolder/LozalizeOutput$c.txt -m $logfolder/timeMeasure$c.txt > $logfolder/log$c.txt) &> $logfolder/execTime$c.txt
    echo "$((c+1)) test:"
read -r output < $logfolder/DetectOutput$c.txt
if [ "$output" = "true" -a "$expectedoutput" = "true" ]; then
    echo "true positive $output <> $expectedoutput"
((truepos++))
elif [ "$output" = "true" -a "$expectedoutput" = "false" ]; then
    echo "false postive $output <> $expectedoutput"
    ((falsepos++))
elif [ "$output" = 'false' -a "$expectedoutput" = 'true' ]; then
    echo "false negative $output <> $expectedoutput"
    ((falseneg++))
    elif [ "$output" = 'false' -a "$expectedoutput" = 'false' ]; then
    echo "true negative $output <> $expectedoutput"
    ((trueneg++))
    else
    echo "failure in execution"
    ((failure++))
    fi
#grep -F -i -n "$expectedoutput " output$c.txt 
((c=c+3))
done

echo "here are some stats: "
echo "failure: $failure"
echo "true +: $truepos"
echo "true -: $trueneg"
echo "false +: $falsepos"
echo "false -: $falseneg"
echo "precision:"
python -c "print $truepos*1.0/($truepos+$falsepos)"
echo "recall:"
python -c "print $truepos*1.0/($truepos+$falseneg)"
