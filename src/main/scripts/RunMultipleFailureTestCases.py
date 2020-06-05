#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import os
import re
import shutil
import sqlite3 as lite
import populateFixDB as db
from subprocess import *
from reportlab.lib.PyFontify import pat

DBNAME = "Fixes.db"
logFolder = "log"
fixesXMLFile = "fixes.xml"
TEMP_AUTO_FIX_FILE_NAME = "TEMP_AUTO_FIXED_FILE.html"
allRankingsFile = os.path.join(logFolder,"allRanking.txt")

def jarWrapper(*args):
    process = Popen(['java', '-jar']+list(args), stdout=PIPE, stderr=PIPE)
    ret = []
    while process.poll() is None:
        line = process.stdout.readline()
        if line != '' and line.endswith('\n'):
            ret.append(line[:-1])
    stdout, stderr = process.communicate()
    ret += stdout.split('\n')
    if stderr != '':
        ret += stderr.split('\n')
    ret.remove('')
    return ret



def runTestCase(testCaseIdx, basepath, baseline , PUT , failures):
		#file represented by PUTFullPath will be changed in each iteration after fixing a failure
	PUTFullPath = basepath + PUT
	baselineFullPath = basepath + baseline;

	print "running test case# ", testCaseIdx
	print "   baseline     : ", baselineFullPath
	print "   PUT          : ", PUTFullPath
	print "   knownFailures: "
	for failure in failures:
		print "		"+failure;
	smallestRank = -1
	runNumber = 0;
	
	
	while smallestRank != sys.maxsize:
		runNumber = runNumber + 1
		logFile =      logFolder+"/"+"log"           +str(testCaseIdx)+"_"+str(runNumber)+".txt"
		detectFile =   logFolder+"/"+"DetectOutput"  +str(testCaseIdx)+"_"+str(runNumber)+".txt"
		localizeFile = logFolder+"/"+"LozalizeOutput"+str(testCaseIdx)+"_"+str(runNumber)+".txt"

		smallestRank = sys.maxsize
		smallestXpath = ""		
		args = ["../../target/LayoutModel-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "-b"+baselineFullPath, "-t"+PUTFullPath, "-d"+detectFile, "-l"+localizeFile]
		result = jarWrapper(*args)
		writeContentToFile(logFile, result)
		for groundTruth in failures:
			reportedRank = getReportedRank(groundTruth, localizeFile)
			if reportedRank < smallestRank:
				smallestRank = reportedRank
				smallestXpath = groundTruth
		
		if smallestRank != sys.maxsize:
			print "   Assigned failure <"+smallestXpath+">"+" rank No: ", smallestRank;
			with open(allRankingsFile,'ab') as f: f.write(str(smallestRank)+'\n')
			print "   now fixing it..."
			PUTFullPath = fixFault(PUTFullPath,smallestXpath,PUT)
			failures.remove(smallestXpath)
			print "   Remaining Failures: "
			for failure in failures:
				print "		"+failure;

			
	if len(failures) > 0:
		print "    some failures were not detected"
		print failures
	
	# deleting created temp file
	head, tail = os.path.split(PUTFullPath[7:])
	tempPagePath = os.path.join(head,TEMP_AUTO_FIX_FILE_NAME)
 	os.remove(tempPagePath)
	print "    deleting created temp page "+tempPagePath
	
	return


def getReportedRank(groundTruth , resultSetFile):
	num = 0;
	with open(resultSetFile) as resultFile:	
		for num, line in enumerate(resultFile, 1):
			if groundTruth.upper() in line.upper():
				print '    found <' + groundTruth + '> at line:', num
				break
	#resultSetFileSize = sum(1 for line in open(resultSetFile))	
	return num
            
            
            
	
def getFixPattern(page,xpath):
	print "    searching for a fix for:" + page
	print "                      xpath:" + xpath
	
	con = lite.connect(DBNAME)    
	
	with con:
		con.row_factory = lite.Row
		cur = con.cursor() 
		cur.execute("SELECT * FROM Fixes WHERE Page = ? AND XPath = ?",(page,xpath))
		row = cur.fetchone()
		
		if row == None:
			print "     fix not found in DB"
			return ('XXXXXXXXXXX','XXXXXXXXXXX')
		else:
			pattern = row["Pattern"]
			replacement = row["Replacement"]
			print "     found fix:"
			print "     pattern: %s Replacement: %s" % (pattern, replacement)

			return (pattern,replacement)

	

	
def fixFault(pageToBeFixed,xpath,pagePath):
	PUTfilepath = pageToBeFixed[7:] # removing "file://"
	head, tail = os.path.split(PUTfilepath)
	newPath = os.path.join(head,TEMP_AUTO_FIX_FILE_NAME)
	if tail != TEMP_AUTO_FIX_FILE_NAME:
		shutil.copyfile(PUTfilepath, newPath)
	pattern , replacement = getFixPattern(pagePath,xpath)
	file_replace(newPath,pattern.encode('utf-8'),replacement.encode('utf-8'))

	return "file://"+newPath



def file_replace(fname, pat, s_after):
    # first, see if the pattern is even in the file.
    
    with open (fname, "r") as myfile:
		fileContent =myfile.read()
		if not re.search(pat, fileContent, flags=re.UNICODE):
			print "     pattern <" + pat + "> not found. could not fix the issue !!"
			return # pattern does not occur in file so we are done.
		else:
			out_fname = fname + ".tmp"
			out = open(out_fname, "w")
			out.write(re.sub(pat, s_after, fileContent, flags=re.UNICODE))
			print "     replaced <" + pat + "> with <" + s_after + ">. "
			out.close()
			os.rename(out_fname, fname)


def writeContentToFile(pathToFile, content):
	with open(pathToFile, 'w') as f:
		for row in content:
			f.write("%s\n" % str(row))





#delete log folder
#create new log folder
with open(sys.argv[1]) as f:
	lines = f.readlines()

if not os.path.exists(logFolder):
    os.makedirs(logFolder)
if os.path.exists(allRankingsFile):
	os.remove(allRankingsFile)

for index,line in enumerate(lines):
	lines[index] = line.rstrip();


db.populateDB()
basepath = "file://"+lines[1]
i = 2;
while i < len(lines):
	baseline = lines[i]
	PUT = lines[i+1]
	numberOfFailures = int(lines[i+2])
	failuresStartIdx = i + 3
	failuresEndIdx = failuresStartIdx + numberOfFailures
	failures  = lines[failuresStartIdx:failuresEndIdx]
	runTestCase(i, basepath , baseline , PUT ,failures)
	i = failuresEndIdx

