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


plt.plot(getX(),getEn())
plt.yscale("log")

#b)

def make_lu(matrix):
    n = np.shape(matrix)[0]
    l = np.eye(n)
    u = matrix

    for i in range(n):
        for j in range(i+1,n):
            if (u[i,i] != 0):
                l[j,i] = u[j,i]/u[i,i]
            u[j] -= l[j,i]*u[i]
    return l,u

def forward_substraction(l,b):
    q = np.array(l)
    n = np.shape(q)[0]
    y = np.zeros(n)
    for i in range(n):
        y[i] = b[i]-sum(q[i][:i])
        q[:,[i]]*=y[i]

    return y


def backward_substarction(u,b):
    q = np.array(u)
    n = np.shape(q)[0]
    x = np.zeros(n)
    for i in range(n-1,-1,-1):
        x[i] = (b[i]-sum(q[i][i+1:]))/q[i][i]
        q[:,[i]] *= x[i]

    return x


def getSelfEn():
    en = np.zeros(36)
    for i in range(5,41):
        a = makeHilbert(i)
        b = makeBn(i)
        l,u = make_lu(a)
        x = backward_substarction(u,forward_substraction(l,b))
        dist = np.linalg.norm(makeXn(i)-x)
        eux = np.linalg.norm(makeXn(i))
        en[i-5] = dist/eux

    return en


plt.plot(getX(),getSelfEn())
plt.show()
