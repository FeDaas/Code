import numpy as np
import random

from matplotlib import pyplot as plt


def decomposevector(x):
    u = np.zeros(int(len(x)/2),dtype=np.complex_)
    v = np.zeros(int(len(x)/2),dtype=np.complex_)

    for n in range(len(x)):
        if n%2 == 0:
            u[n//2] = x[n]
        else:
            v[(n-1)//2] = x[n]

    return u,v


def compute_inverse_factors(n):
    inv_omega = np.zeros((n+1)//2,dtype=np.complex_)
    for i in range(len(inv_omega)):
        inv_omega[i]=1/(np.exp((2*np.pi*1j*i)/(n+1)))

    return inv_omega


def compute_Fx(Fu, Fv):
    Fx = np.zeros(len(Fv)*2,dtype=np.complex_)

    for i in range(len(Fu)):
        Fx[i] = 0.5*Fu[i] + 0.5*compute_inverse_factors(len(Fu)*2-1)[i]*Fv[i]
        Fx[len(Fu)+i] =  0.5*Fu[i] - 0.5*compute_inverse_factors(len(Fv)*2-1)[i]*Fv[i]

    return Fx


d54


x = np.array([0,1,0,1,0,1,0,1],dtype=np.complex_)
n = len(x)-1
Fu = np.array([0,1,2,3],dtype=np.complex_)
Fv = np.array([4,5,6,7],dtype=np.complex_)

u,v = decomposevector(x)
inv_omega = compute_inverse_factors(n)
Fx = compute_Fx(Fu,Fv)
Fx2 = fft([0,1,2,3,4,5,6,7])

print(u)
print("---")
print(v)
print("---")
print(inv_omega)
print("---")
print(Fx)
print("---")
print(Fx2)


z = np.linspace(0, 2 * np.pi, 2**10)
x1 = np.sin(z)
x2 = np.sin(z) + np.sin(40 * z) + np.sin(100 * z)
x3 = np.sin(10 * z) + 0.5 * random.uniform(0, 1)

c1 = fft(x1)
c2 = fft(x2)
c3 = fft(x3)

real1 = np.real(c1)
real2 = np.real(c2)
real3 = np.real(c3)

imag1 = np.imag(c1)
imag2 = np.imag(c2)
imag3 = np.imag(c3)

plt.title("sin(z)")
plt.plot(z, real1, color='r', label="Real")
plt.plot(z, imag1, color='b', label="Imag")
plt.yscale("log")
plt.ylabel("y")
plt.xlabel("x")
plt.legend()
plt.show()

plt.title("sin(z) + sin(40z) + sin(100z)")
plt.plot(z, real2, color='r', label="Real")
plt.plot(z, imag2, color='b', label="Imag")
plt.yscale("log")
plt.ylabel("y")
plt.xlabel("x")
plt.legend()
plt.show()

plt.title("sin(10z) + 0.5 * random.uniform(0, 1)")
plt.plot(z, real3, color='r', label="Real")
plt.plot(z, imag3, color='b', label="Imag")
plt.yscale("log")
plt.ylabel("y")
plt.xlabel("x")
plt.legend()
plt.show()
