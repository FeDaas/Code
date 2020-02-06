import numpy as np
import matplotlib.pyplot as plt

def Richardson(A, b, D, x0, max_iter):
    I = np.eye(A.shape[0])

    xi = np.zeros((max_iter, A.shape[1]))
    xi[0] = np.dot((I - np.dot(D, A)), x0) + np.dot(D, b)
    for i in range(1,max_iter):
        xi[i] = np.dot((I - np.dot(D, A)), xi[i-1]) + np.dot(D, b)

    return xi


def createMatrixB(m):
    B = np.zeros((m, m))

    B[0, 0] = 4
    B[0, 1] = -1


    for i in range(1, m-1):
        B[i, i-1] = -1

        B[i, i] = 4
        B[i, i+1] = -1

    B[m-1, m-2] = -1

    B[m-1, m-1] = 4

    return B


def createNegativeI(m):
    I_negative = np.zeros((m, m))
    for i in range(m):

        I_negative[i, i] = -1

    return I_negative


def createMatrixA(m):

    n = m * m
    B = createMatrixB(m)
    I_negative = createNegativeI(m)
    I_B_I = np.concatenate((I_negative, B, I_negative), axis=1)

    A = np.zeros((n, n))


    A[:m, :2*m] = I_B_I[:, m:]


    for i in range(m, n-m, m):
        A[i:i+m, i-m:i+2*m] = I_B_I

    A[n-m:n, n-2*m:n] = I_B_I[:, :2*m]

    return A


A = createMatrixA(10)
b = np.ones(100)
x0 = np.zeros(100)
xk = np.linalg.solve(A, b)

# a)

axi = Richardson(A, b, np.eye(100), x0, 10)

aEk = np.zeros(axi.shape[0])
for i in range(axi.shape[0]):
    aEk[i] = np.linalg.norm(axi[i] - xk)


# b)

bxi = Richardson(A, b, np.eye(100)*0.2, x0, 500)

bEk = np.zeros(bxi.shape[0])
for i in range(bxi.shape[0]):
    bEk[i] = np.linalg.norm(bxi[i] - xk)



# c)

cxi = Richardson(A, b, np.linalg.inv(np.diag(np.diagonal(A))), x0, 500)

cEk = np.zeros(cxi.shape[0])
for i in range(cxi.shape[0]):
    cEk[i] = np.linalg.norm(cxi[i] - xk)



plt.plot(range(1,11),aEk,color="r",label="D1")
plt.plot(range(1,501),bEk,color="g",label="D2")
plt.plot(range(1,501),cEk,color="b",label="D3")

plt.xlabel("Iterationsschritte")
plt.ylabel("Fehler")
plt.yscale("log")
plt.legend()
plt.show()
