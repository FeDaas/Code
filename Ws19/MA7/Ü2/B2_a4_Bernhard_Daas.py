import matplotlib.pyplot as plt
import math


def calculateError(h):
    f_deriv_0 = 1
    g_h = ((math.exp(math.sin(h)) - 1) / h)
    error = abs(f_deriv_0 - g_h)

    return error

def aufgabe_a():
    for x in range(0, -15, -1):
        print("---------------------------------------")
        print("h = " + str(math.pow(10,x)) + ":")
        print(str(calculateError(math.pow(10, x))))
        print()

def aufgabe_b():
    values_x = list()
    values_y = list()

    for x in range(0, -15, -1):
        values_x.append(math.pow(10, x))
        values_y.append(calculateError(math.pow(10, x)))

    plt.plot(values_x, values_y)
    plt.yscale("log")
    plt.xscale("log")

    plt.title("Aufgabe 4")
    plt.ylabel("| f\'(0) - g(h) |")
    plt.xlabel("h")
    plt.show()


if __name__ == "__main__":
    aufgabe_a()
    aufgabe_b()
