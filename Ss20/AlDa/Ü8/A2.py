def hhash(s):
    h = 0
    for k in s:
        h = 23*h + ord(k)
    return h

def findSame():
    strings = []
    hash = [[0,[]] for x in range(11448000) ]
    for x1 in [x for x in range(65,123) if (x < 91 or x > 97 )]:
        for x2 in [x for x in range(65,123) if (x < 91 or x > 97 )]:
            for x3 in [x for x in range(65,123) if (x < 91 or x > 97 )]:
                for x4 in [x for x in range(65,123) if (x < 91 or x > 97 )]:
                    strings.append(chr(x1)+chr(x2)+chr(x3)+chr(x4))

    for word in strings:
        h = hhash(word)
        #print(h,word)
        hash[h][0] += 1
        hash[h][1].append(word)

    for word in hash:
        if(word[0] > 16):
            print(word)
            break


findSame()
