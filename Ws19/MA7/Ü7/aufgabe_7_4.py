import numpy as np
import math
import matplotlib.pyplot as plt
import random
from PIL import Image

def build_kernel(g,N,M):
  G = np.zeros((M,N))
  n,m = np.shape(g)

  b = int(math.floor(0.5 * n))

  for l in range(-b,b+1):
    for k in range(-b,b+1):
      G[k,l] = g[k+b,l+b]

  return G

def print_matrix(X):
  m, n = np.shape(X)

  for i in range(0,m):
    for j in range(0,n):
      print(round(np.real(X[i,j]),3), "+j*", round(np.imag(X[i,j]),3), ", ", end='')
    print("\n")

  print("\n")

def decompose_vector (x):
  n = len(x) - 1
  assert ( n % 2 == 1 )

  half_n = int(0.5 * (n+1))
  #print ("decompose vector of length", len(x), " into 2 pieces of size", half_n)

  u = np.zeros(half_n, dtype=np.complex_)
  v = np.zeros(half_n, dtype=np.complex_)

  for j in range(0, half_n):
    u[j] = x[2*j]
    v[j] = x[2*j+1]

  return u, v

def compute_inverse_factors(n):
  assert ( n % 2 == 1 )
  max_k = int (0.5 * (n+1))

  inv_omega = np.zeros(max_k, dtype=np.complex_)

  for k in range(0, max_k):
    arg = 2. * math.pi * float(k) / (float(n)+1)
    inv_omega[k] = math.cos( arg ) - 1j * math.sin ( arg )

  return inv_omega

def compute_factors(n):
  assert ( n % 2 == 1 )
  max_k = int (0.5 * (n+1))

  omega = np.zeros(max_k, dtype=np.complex_)

  for k in range(0, max_k):
    arg = 2. * math.pi * float(k) / (float(n)+1)
    omega[k] = math.cos( arg ) + 1j * math.sin ( arg )

  return omega

def compute_Fx (Fu, Fv):
  n = int(2 * len(Fu) - 1)

  half_n = int(0.5 * (n-1))

  Fx = np.zeros(n+1, dtype=np.complex_)

  inv_omega = compute_inverse_factors(n)

  for k in range(0, half_n+1):
    Fx[k] = 0.5 * Fu[k] + 0.5 * inv_omega[k] * Fv[k]
    Fx[half_n + 1 + k] = 0.5 * Fu[k] - 0.5 * inv_omega[k] * Fv[k]

  return Fx

def fft(x):
  n = len(x) - 1
  #print ("FFT for n = ", n)
  assert ( n % 2 == 1 or n == 0)

  if n == 0:
    return x

  #if np.max(np.abs(x)) < 1e-16:
  #  return np.zeros(n+1)

  u,v = decompose_vector(x)

  #print ("len(u)=", len(u))
  #print ("len(v)=", len(v))

  Fu = fft(u)
  Fv = fft(v)

  Fx = compute_Fx(Fu,Fv)

  return Fx

def ifft(x):
  n = len(x) - 1
  #print ("FFT for n = ", n)
  assert ( n % 2 == 1 or n == 0)

  if n == 0:
    return x

  #if np.max(np.abs(x)) < 1e-16:
  #  return np.zeros(n+1)

  u,v = decompose_vector(x)

  #print ("len(u)=", len(u))
  #print ("len(v)=", len(v))

  Fu = ifft(u)
  Fv = ifft(v)

  Fx = np.zeros(n+1, dtype=np.complex_)
  half_n = int(0.5 * (n-1))
  omega = compute_factors(n)

  for k in range(0, half_n+1):
    Fx[k] = Fu[k] + omega[k] * Fv[k]
    Fx[half_n + 1 + k] = Fu[k] - omega[k] * Fv[k]

  return Fx

def fft2(X):
    #Aufgabe 1
    m,n = np.shape(X)[0],np.shape(X)[1]
    Z = np.zeros((m,n),dtype=np.complex_)
    C = np.zeros((m,n),dtype=np.complex_)
    for i in range(n):
        Z[:,i]= fft(X[:,i])

    for i in range(m):
        C[:,i] = fft(Z[i])

    return C


def ifft2(C):
    pass
  # Aufgabe b

def convolution(X,Y):
    pass
  # Aufgabe c






#####################
# a
X = np.eye(4)
C = fft2(X)
print_matrix(C)

'''
#####################
# b
Cinv = ifft2(C)
print_matrix(Cinv)

#####################
# c
X = np.eye(4)
G = np.zeros((4,4))
G[0,0] = 2
G[0,1] = -1
XG = convolution(X, G)
print_matrix(XG)

######################
# d

# lade bilddatei
img = Image.open( "Lenna.png" )
img.load()
I = np.asarray( img, dtype="int32" )

# extrahiere roten Farbkanal -> Array X
X = I[:,:,0]
M, N = np.shape(X)
print ("Original Bild")
plt.clf()
plt.imshow(X)
plt.show()

# erzeuge Gauss Filter zur Weichzeichnung
g5 = np.array([[1., 4., 7., 4., 1.],
              [4., 16., 26., 16., 4.],
              [7., 26., 41., 26., 7.],
              [4., 16., 26., 16., 4.],
              [1., 4., 7., 4., 1.]]) * (1. / 273.)
G = build_kernel(g5, N,M)

# berechne Fourier Koeffizienten von X
C = fft2(X)

print ("Spektralanalyse von Original Bild")
plt.clf()
plt.imshow(np.log(np.abs(C)), cmap = "RdBu")
plt.show()

# Berechne Faltung von X mit Kernel
XG = convolution(X, G)

print ("Weichzeichnung")
plt.clf()
plt.imshow(np.real(XG))
plt.show()

# Berechne Differenz zwischen Originalbild X und Weichzeichnung
XE = X - XG

print ("Hervorhebung von Kanten")
plt.clf()
plt.imshow(np.real(XE))
plt.show()
'''
