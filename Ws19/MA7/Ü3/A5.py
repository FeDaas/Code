from numpy import array, append, vstack, exp, sin, linspace, float64, pi, copy
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

def calculateN(x, z):
    n = list()
    for j in range(len(z)):
        nz = [1]
        for i in range(1,len(x)):
            nz.append(nz[i-1]*(z[j]-x[i-1]))
        n.append(nz)
    return array(n)


def NewtonCoeff(x, y):
    coeff = copy(y)
    for i in range(len(coeff)):
        for j in range(len(coeff)-1, i, -1):
            coeff[j] = (coeff[j] - coeff[j-1]) / (x[j] - x[j-1-i])

    return coeff

def evalNewtonCoeff(c, x, z):
    n = calculateN(x, z)
    p = list()
    for zi in range(len(z)):
        sum = 0
        for i in range(len(x)):
            sum += c[i] * n[zi, i]
        p.append(sum)
    return array(p,dtype=float64)

def getRange(a, b, n):
    x_list = list()
    for i in range(n):
        x_list.append(a + (i/n) * (b - a))

    return array(x_list, dtype=float64)

def printer(x, y, c, p):
    placeholder = "-------------------------------------------------------------------------------------"
    placeholder_plus = "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

    print("\n" + placeholder_plus)
    print("Values from exercise 4:")
    print(placeholder_plus)
    print("{:<25}".format("\nx values:") + f"{x}")
    print(placeholder)
    print("{:<25}".format("\ny values:") +  f"{y}")
    print(placeholder)
    print("{:<25}".format("\nncoefficients:") + f"{c}")
    print(placeholder)
    print("{:<25}".format("\nevaluated results:") + f"{p}")
    print(placeholder + "\n")

def plotter(f_x, p_list, y_label, title):
    red_patch = mpatches.Patch(color='red', label="f(x)")
    green_patch = mpatches.Patch(color='green', label="n = 5")
    blue_patch = mpatches.Patch(color='blue', label="n = 11")
    yellow_patch = mpatches.Patch(color='yellow', label="n = 14")

    plt.plot(z, p_list[0], 'g')
    plt.plot(z, p_list[1], 'b')
    plt.plot(z, p_list[2], 'y')
    plt.plot(z, f_x, 'r')
    plt.xlabel("x")
    plt.ylabel(y_label)
    plt.legend(handles=[red_patch, green_patch, blue_patch, yellow_patch])
    plt.title(title)
    plt.show()

##############################################################################################################

if __name__ == "__main__":

    # Values from exercise 4
    x = array([0, 1, 2, 3], dtype=float64)
    y = array([-6, 2, 1, 3], dtype=float64)
    z = array([0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5], dtype=float64)
    c = NewtonCoeff(x, y)
    p = evalNewtonCoeff(c, x, z)

    printer(x, y, c, p)

    # Values from exercise 5c
    n_list = [5, 11, 14]

    red_patch = mpatches.Patch(color='red', label="f(x)")
    green_patch = mpatches.Patch(color='green', label="n = 5")
    blue_patch = mpatches.Patch(color='blue', label="n = 11")
    yellow_patch = mpatches.Patch(color='yellow', label="n = 14")

    # (a)
    a = 0
    b = pi * 2
    z = linspace(a, b, 300)
    f_x = sin(z)

    p_list = list()

    for n in n_list:
        x = getRange(a, b, n)
        y = sin(x)
        c = NewtonCoeff(x, y)
        p = evalNewtonCoeff(c, x, z)
        p_list.append(p)

    plotter(f_x, p_list, "sin(x)", "Aufgabe 5c (a)")

    # (b)
    a = 0
    b = 2
    z = linspace(a, b, 300)
    f_x = exp(z)

    p_list = list()

    for n in n_list:
        x = getRange(a, b, n)
        y = exp(x)
        c = NewtonCoeff(x, y)
        p = evalNewtonCoeff(c, x, z)
        p_list.append(p)

    plotter(f_x, p_list, "exp(x)", "Aufgabe 5c (b)")

    # (c)
    a = -1
    b = 1
    z = linspace(a, b, 300)
    f_x = 1.0 / (1.0 + (5*z)**2)

    p_list = list()

    for n in n_list:
        x = getRange(a, b, n)
        y = 1.0 / (1.0 + (5*x)**2)
        c = NewtonCoeff(x, y)
        p = evalNewtonCoeff(c, x, z)
        p_list.append(p)

    plotter(f_x, p_list, "1 / (1 + (5x)**2)", "Aufgabe 5c (c)")
