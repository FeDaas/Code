def fib1(n):
    if n <=1:
        return n
    return fib1(n-1) + fib1(n-2)

def fib3Impl(n):
    if n == 0:
        return 1, 0
    else:
        f1, f2 = fib3Impl(n-1)
        return f1 + f2, f1

def fib3(n):
    f1, f2 = fib3Impl(n)
    return f2

def fib5(n):
    f1, f2 = 1, 0
    while n > 0:
        f1, f2 = f1 + f2, f1
        n -= 1
    return f2

def test_fib():
    assert (89 == fib1(11))
    assert (102334155 == fib3(40))
    assert (102334155 == fib5(40))
    assert (102334155 == fib6(40))
    assert (102334155 == fib7(40))
    assert (fib1(20) == fib3(20) == fib5(20) == fib6(20) == fib7(20))

def mul2x2(mat1, mat2):
    mat3 = [[],[],[],[]]
    mat3[0] = mat1[0]*mat2[0] + mat1[1]*mat2[2]
    mat3[1] = mat1[0]*mat2[1] + mat1[1]*mat2[3]
    mat3[2] = mat1[2]*mat2[0] + mat1[3]*mat2[2]
    mat3[3] = mat1[2]*mat2[1] + mat1[3]*mat2[3]
    return mat3

def fib6(n):
    mat1 = [1,1,1,0]
    matn = [1,1,1,0]
    for x in range(n-1):
        matn = mul2x2(matn, mat1)

    return matn[1]

def recMatPot(mat,n):
    if(n == 0):
        return [1,0,0,1]
    if(n == 1):
        return mat
    if(n % 2 == 0):
        return recMatPot(mul2x2(mat,mat),n/2)
    if(n % 2 == 1):
        return mul2x2(mat,recMatPot(mul2x2(mat, mat),(n-1)/2))

def fib7(n):
    mat1 = [1,1,1,0]
    return recMatPot(mat1,n)[1]

num =fib7(5500000)
print(len(str(num)))

'''
fib1() = 10sec für 36 (immer neuer funktionsaufruf)
fib3() = max recursion dec über 996 (zu viele funktionsaufrufe)
fib5() = 10sec für 700000 (ich hab keine ahnung)
fib6() = 10sec für 200000 (s.o.)
fib7() = 10sec für 5500000 (s.0.) 1149432 Ziffern

'''
