import random
import timeit
from math import sqrt, ceil
import matplotlib.pyplot as plt

def create_data(size):
    a = []
    while len(a) < size:
        x, y = random.uniform(-1, 1), random.uniform(-1, 1)
        r = sqrt(x**2 + y**2)
        if r < 1.0:
            a.append(r)
    return a

def naiveQuantize(r, M):
    return int(r*M)

def quantize(r, M):
    return int((r**2)*M)

def naiveFillBucket(n, M):
    buckets =  [[] for x in range(M)]
    for i in n:
        buckets[naiveQuantize(i,M)].append(i)

    return buckets

def fillBucket(n, M):
    buckets =  [[] for x in range(M)]
    for i in n:
        buckets[quantize(i,M)].append(i)

    return buckets

def getKeyCount(buckets):
    count = 0
    for x in buckets:
        count += len(x)
    return count

def chi(buckets):
    chi = 0.
    keys = 0
    c = getKeyCount(buckets)/len(buckets)

    for k in range(len(buckets)):
        chi += (((len(buckets[k])-c)**2)/c)

    return chi

def chi_squared(buckets):

    return 3 > sqrt(2*chi(buckets))-sqrt(2*len(buckets)-3)

def test_chi():
    pass
    #print("N = 5, M = 5")
    #print(chi_squared(fillBucket(create_data(5),5)))

    #print("N = 5, M = 5 naive")
    #print(chi_squared(naiveFillBucket(create_data(5),5)))

    #print("N = 50, M = 5")
    #print(chi_squared(fillBucket(create_data(50),5)))

    #print("N = 50, M = 5 naive")
    #print(chi_squared(naiveFillBucket(create_data(50),5)))

    #print("N = 50, M = 50")
    #print(chi_squared(fillBucket(create_data(50),50)))

    #print("N = 50, M = 50 naive")
    #print(chi_squared(naiveFillBucket(create_data(50),50)))

    #print("N = 1000, M = 10")
    #print(chi_squared(fillBucket(create_data(1000),10)))

    #print("N = 1000, M = 10 naive")
    #print(chi_squared(naiveFillBucket(create_data(1000),10)))

def bucketSort(list, c = 2, naive = True):
    sortedList = []
    n = len(list)
    m = ceil((n/c))
    if(naive):
        buckets = naiveFillBucket(list, m)
    else:
        buckets = fillBucket(list, m)
    for bucket in buckets:
        bucket.sort()
        sortedList.append(bucket)

    return sortedList

def plot_times():
    print("Plotte Zeiten...")
    _bucketSort = bucketSort
    times = []
    badTimes = []

    for n in range(500):
        time1 = 0
        time2 = 0
        for _ in range(10):
            data1 = create_data(n)
            data2 = data1.copy()
            time1 += timeit.timeit("_bucketSort(data1)", globals = locals(), number=1)
            time2 += timeit.timeit("_bucketSort(data2, naive = False)", globals = locals(), number=1)
        times.append(time1 / 10)
        badTimes.append(time2 / 10)

    plt.plot(range(500), times, label = "index = int(r**2 * M)")
    plt.plot(range(500), badTimes, label= "index = int(r * M)")
    plt.title("Bucket Sort")
    plt.xlabel("n")
    plt.ylabel("time")
    plt.legend()
    plt.show()

plot_times()
