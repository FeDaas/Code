import matplotlib.pyplot as plt
import numpy as np
import scipy.linalg
from scipy.linalg import lu_factor, lu_solve

def makeHilbert(size):
    hil = np.zeros((size,size))
    for i in range(1,size+1):
        coll = np.zeros(size)
        for j in range(1,size+1):
            coll[j-1] = 1/(i+j-1)
        hil[i-1] = coll

    return hil

def makeXn(size):
    xn = np.zeros(size)
    for i in range(size):
        xn[i] = 1

    return xn

def makeBn(size):
    hil=makeHilbert(size)
    bn = np.zeros(size)
    for i in range(size):
        bn[i] = sum(hil[i])

    return bn

def getEn():
    en = np.zeros(36)
    for i in range(5,41):
        a = makeHilbert(i)
        b = makeBn(i)
        lu, piv = lu_factor(a)
        dist = np.linalg.norm(makeXn(i)-(lu_solve((lu,piv),b)))
        eux = np.linalg.norm(makeXn(i))
        en[i-5] = dist/eux
    return en

def getX():
    x = np.zeros(36)
    for i in range(36):
        x[i] = i+5

    return x

a = makeHilbert(40)
b = makeBn(40)


lu, piv = lu_factor(a)
dist = lu_solve((lu,piv),b)

print(getEn())

plt.plot(getX(),getEn())
plt.yscale("log")
plt.show()
