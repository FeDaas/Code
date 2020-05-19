import math

def archimedes1(k):
    s = math.sqrt(2)
    t = 2.
    n = 4.
    for i in range(k):
        s = math.sqrt((2-(math.sqrt(4-s**2))))
        t = 2/t*(math.sqrt(4+t**2)-2)
        n = n*2
        #print(n, n/2*s, n/2*t, n/2*t-n/2*s)
    return s,t

def archimedes2(k):
    s = math.sqrt(2)
    t = 2.
    n = 4.
    for i in range(k):
        s = s/(math.sqrt(2+(math.sqrt(4-s**2))))
        t = 2*t/((math.sqrt(4+t**2)+2))
        n = n*2
        #print(n, n/2*s, n/2*t, n/2*t-n/2*s)
    return s,t

def test(k):
    n = 8
    for i in range(1,k):

        s1,t1 =archimedes1(i)
        s2,t2 =archimedes2(i)
        t1C = 2*s1/math.sqrt(4-s1**2)
        t2C = 2*s2/math.sqrt(4-s2**2)
        s1 = n/2*s1
        t1 = n/2*t1
        s2 = n/2*s2
        t2 = n/2*t2
        t1C = n/2*t1C
        t2C = n/2*t2C

        if (t1 > t1C-0.0000000000001 and t1 < t1C+0.0000000000001):
            test1 = "true"
        else:
            test1 = "false"
        if (t2 > t2C-0.0000000000001 and t2 < t2C+0.0000000000001):
            test2 = "true"
        else:
            test2 = "false"


        print(n,"\tKanten: A1s:",s1," \tA1t:",t1,"  \tA1tC:",t1C," ",test1,"\n\t\tA2s:",s2,"   \tA2t:",t2,"  \tA1tC:",t1C," ",test2)

        n = n*2



#archimedes1(9)
#archimedes2(99)
test(17)



'''
b)
    Bei N-ecken mit geringer Seitenlängen kann Auslöschung Auftretenself.
    Besonders gefährich sind Subtraktionen mit sehr kleinen werten. Da sn und
    tn immer kleiner werden, werden diese werte "ignoriert". Dann lautet die
    verfälschte Formel s2n = sqrt(2-2) und t2n = 2/tn*(sqrt(4)-2), die sogar einen
    Mathefehler auslösen



             sqrt(2-sqrt(4-sn^2)) = sn/sqrt(2+sqrt(4-sn^2))
                   2-sqrt(4-sn^2) = sn^2/(2+sqrt(4-sn^2))
(2-sqrt(4-sn^2))*(2+sqrt(4-sn^2)) = sn^2
               4-(sqrt(4-sn^2)^2) = sn^2
                       4-(4-sn^2) = sn^2
                         4-4+sn^2 = sn^2
                             sn^2 = sn^2

                2/tn * (sqrt(4+tn^2)+2) = 2tn/(sqrt(4+tn^2)+2)
2/tn * (sqrt(4+tn^2)+2)(sqrt(4+tn^2)+2) = 2tn
                               4+tn^2-4 = tn^2

Diese Formel sind besser da sie die probleme die bei Antwort a) beschrieben wurde
beheben. Es steht für kleine sn keine 0 mehr in der Würzel und bei tn giebt es
keine subtraktion mehr.

Am Anfang bekommt man eine Stelle pro verdopplung. Danach verdoppelt sich in
etwa die Anzahl an verdoplungen um eine weitere Stelle von Pi zu erreichen


'''
