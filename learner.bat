@ECHO OFF

SET /a NUMBER=%1

java -cp ".\bin" Paxos learner %NUMBER%