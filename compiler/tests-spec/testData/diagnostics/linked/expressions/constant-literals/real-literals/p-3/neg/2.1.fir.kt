// TESTCASE NUMBER: 1
val value_1 = <!ILLEGAL_CONST_EXPRESSION!>0e<!>

// TESTCASE NUMBER: 2
val value_2 = <!ILLEGAL_CONST_EXPRESSION!>00e-<!>

// TESTCASE NUMBER: 3
val value_3 = <!ILLEGAL_CONST_EXPRESSION!>000E+<!>

// TESTCASE NUMBER: 4
val value_4 = <!ILLEGAL_CONST_EXPRESSION!>0000e+<!>

// TESTCASE NUMBER: 5
val value_5 = <!ILLEGAL_CONST_EXPRESSION!>00000000000000000000000000000000000000E<!>

// TESTCASE NUMBER: 6
val value_6 = <!ILLEGAL_CONST_EXPRESSION!>34567E+<!>

// TESTCASE NUMBER: 7
val value_7 = <!ILLEGAL_CONST_EXPRESSION!>456e-<!>

// TESTCASE NUMBER: 8
val value_8 = <!ILLEGAL_CONST_EXPRESSION!>55555e+f<!>

// TESTCASE NUMBER: 9
val value_9 = <!ILLEGAL_CONST_EXPRESSION!>666666E-F<!>
