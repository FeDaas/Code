import  numpy as np


a = 0
b = 1
itrue = 1/2*np.log(3)

#((b-a)/n)/2 * ((1/1+2*x)+2*Ei-n-1 f(xi)

for n in range(2,999999,2):
    sum = 0
    for i in range(1,n):
        sum+=1/(1+2*(i/n))

    it = (((b-a)/n)/2) * ((1/(1+2*a))+2 * sum + 1/(1+2*b))
    print(it)

    sum2 = 0

    for i in range(0,n):
        sum2 += 1/(1+2*((i/n+i/(n+1))/2))

    isi = (((b-a)/n)/6) * ((1/(1+2*a))+2 * sum + 4 * sum2 + 1/(1+2*b))
    print(isi)

    if(abs(itrue-it)<10**(-8) and (abs(itrue-isi)<10**(-8))):
        print(n)
        break

    if(n==36):
        print("!!!!!!!")
