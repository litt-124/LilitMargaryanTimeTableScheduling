# TimeTableScheduling
Consider the Unconstrained Examination Timetabling Problem – given N courses, M
students and the lists Sk of students taking each course (1  k  N), distribute the N exams within 
the given number of timeslots T such that no student will take more than one exam in each timeslot.
The optimal solution is achieved when T is minimal.
Consider also the following heuristic algorithm to solve the problem:
1. Specify the number of timeslots T and schedule all of N exams (enumerated from 1 to N) 
for the same first timeslot (the timeslots are enumerated from 0 to T – 1). Obviously, the 
total number of clashes between the exams is maximal in such a single-timeslot schedule.
2. Check if the first exam has a clash. If so, shift it to the next timeslot. If in the next timeslot 
it still has a clash, shift it further to the next timeslot. The number of such shifts s is 
specified at the beginning. If the exam still has a clash after s shits, leave it there and turn 
to the next exam.
3. Complete one full iteration by repeating such checks and shifts for all other exams from 2 
to N.
4. Repeat the iterations n times.
