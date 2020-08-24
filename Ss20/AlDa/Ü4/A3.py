#---------------------------------------------------------------------------------------------------------------------#
#                                                                                                                     #
# IAD Übungsblatt 4, Aufgabe 3 - Bernhard, Daas                                                                       #
#                                                                                                                     #
#---------------------------------------------------------------------------------------------------------------------#

import matplotlib.pyplot as plt
import doctest
import timeit
import pytest
import copy

#---------------------------------------------------------------------------------------------------------------------#

class array_deque:

    def __init__(self):                   # constructor for empty container
        '''
        set size to 1, first and last to 0, capacity to 1 and create data array
        with lenght 1

        >>> a = array_deque()
        >>> a.size()
        0
        >>> a.capacity()
        1
        '''
        self.size_ = 0                    # no item has been inserted yet
        self.capacity_ = 1                # we reserve memory for at least one item
        self.data_ = [None]               # internal memory (init one free cell)
        self.first_ = 0
        self.last_ = self.size_


    def size(self):
        '''
        return the current number of elements

        >>> a = array_deque()
        >>> a.push(3)

        >>> a.size()
        1
        '''
        return self.size_


    def capacity(self):
        '''
        returns the current allocated capacity
        >>> a = array_deque()
        >>> a.capacity()
        1
        '''
        return self.capacity_


    def push(self, item):                 # add item at the end
        '''
        adds the given item at the end of the array_deque and
        doubles the capacity if needed

        >>> a = array_deque()
        >>> a.push(3)
        >>> a.last()
        3
        '''
        if self.capacity_ == self.size_:  # internal memory is full
            old_data = copy.deepcopy(self.data_)
            old_first = self.first_
            old_size = self.size_
            old_capacity = self.capacity_

            self.capacity_ *= 2
            self.data_ = [None] * self.capacity_
            self.first_ = 0
            self.last_ = self.size_ - 1

            for i in range(old_size):
                self.data_[i] = old_data[(old_first + i) % old_capacity]
                i += 1

        self.last_ = ((self.last_ + 1) % self.capacity_ if self.size_ != 0 else 0)
        self.size_ += 1
        self.data_[self.last_] = item


    def pop_first(self):
        '''
        removes the first element and returns it

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> a.pop_first()
        1
        '''
        if self.size_ == 0:
            raise RuntimeError("pop_first() on empty container")

        value = self.data_[self.first_]
        self.data_[self.first_] = None
        self.first_ = (self.first_ + 1) % self.capacity_
        self.size_ -= 1

        return value


    def pop_last(self):
        '''
        removes the last element and returns it

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> a.pop_last()
        2
        '''
        if self.size_ == 0:
            raise RuntimeError("pop_last() on empty container")

        value = self.data_[self.last_]
        self.data_[self.last_] = None
        self.last_ = (self.last_ - 1) % self.capacity_
        self.size_ -= 1

        return value


    def __getitem__(self, index):         # __getitem__ implements v = c[index]
        '''returns item at index'''
        if index < 0 or index >= self.size_:
            raise RuntimeError("index out of range")

        return self.data_[(self.first_ + index) % self.capacity_]


    def __setitem__(self, index, v):      # __setitem__ implements c[index] = v
        '''sets item at index to a given value v'''
        if index < 0 or index >= self.size_:
            raise RuntimeError("index out of range")

        self.data_[(self.first + index) % self.capacity_] = v


    def first(self):
        '''
        returns the first item

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> a.first()
        1
        '''
        return self.data_[self.first_]


    def last(self):
        '''
        returns the last item

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> a.last()
        2
        '''
        return self.data_[self.last_]


    def __eq__(self, other):
        '''
        returns True if self and other have same size and elements

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> b = array_deque()
        >>> b.push(1)
        >>> b.push(2)
        >>> a == b
        True
        '''
        if self.size_ != other.size_:
            return False

        for i in range(self.size_):
            if self.data_[(self.first_ + i) % self.capacity_] != other.data_[(other.first_ + i) % other.capacity_]:
                return False

        return True


    def __ne__(self, other):
        '''
        returns True if self and other have different size or elements

        >>> a = array_deque()
        >>> a.push(1)
        >>> a.push(2)
        >>> b = array_deque()
        >>> b.push(1)
        >>> b.push(3)
        >>> a != b
        True
        '''
        return not (self == other)

#---------------------------------------------------------------------------------------------------------------------#

class slow_array_deque(array_deque):

    def push(self, item):                 # add item at the end
        '''
        adds the given item at the end of the array_deque and
        doubles the capacity if needed
        '''
        if self.capacity_ == self.size_:  # internal memory is full
            old_data = copy.deepcopy(self.data_)
            old_first = self.first_
            old_size = self.size_
            old_capacity = self.capacity_

            self.capacity_ += 1
            self.data_ = [None] * self.capacity_
            self.first_ = 0
            self.last_ = self.size_ - 1

            for i in range(old_size):
                self.data_[i] = old_data[(old_first + i) % old_capacity]
                i += 1

        self.last_ = ((self.last_ + 1) % self.capacity_ if self.size_ != 0 else 0)
        self.size_ += 1
        self.data_[self.last_] = item

#---------------------------------------------------------------------------------------------------------------------#

def test_array_deque():
    '''Testing functionality of the array_deque container.
    Tests for first(), last() and size() are included in the check-functions.'''

    # Test initializing
    d = array_deque()
    assert d.size() == 0
    assert d.size() <= d.capacity()

    # Test push() on empty and filled arrays with start_ == 0
    d = array_deque()
    for i in range(10):
        check_push(d, i)

    # Test push() on empty and filled arrays with start_ != 0
    d = array_deque()
    for i in range(4):
        check_push(d, i)
    for i in range(3):
        check_pop_first(d)
    for i in range(10):
        check_push(d, i)

    # Test pop_last() and pop_first() on filled and empty array and start_
    d_copy_1 = copy.deepcopy(d)
    d_copy_2 = copy.deepcopy(d)
    for _ in range(12):
        check_pop_last(d_copy_1)
        check_pop_first(d_copy_2)

#---------------------------------------------------------------------------------------------------------------------#

def test_slow_array_deque():
    '''Testing functionality of the slow_array_deque container.
    Tests for first(), last() and size() are included in the check-functions.'''

    # Test initializing
    d = slow_array_deque()
    assert d.size() == 0
    assert d.size() <= d.capacity()

    # Test push() on empty and filled arrays with start_ == 0
    d = array_deque()
    for i in range(10):
        check_push(d, i)

    # Test push() on empty and filled arrays with start_ != 0
    d = array_deque()
    for i in range(4):
        check_push(d, i)
    for i in range(3):
        check_pop_first(d)
    for i in range(10):
        check_push(d, i)

    # Test pop_last() and pop_first() on filled and empty array and start_
    d_copy_1 = copy.deepcopy(d)
    d_copy_2 = copy.deepcopy(d)
    for _ in range(12):
        check_pop_last(d_copy_1)
        check_pop_first(d_copy_2)

#---------------------------------------------------------------------------------------------------------------------#

def check_push(data, item):
    '''Check functionality of the push() method.'''
    old_data = copy.deepcopy(data)

    data.push(item)

    check_always_needed(data)

    assert data.size() == old_data.size() + 1
    assert data.last() == item

    if old_data.size() == 0:
        assert data.first() == item
    else:
        assert data.first() == old_data.first()

    for i in range(old_data.size()):
        assert data[i] == old_data[i]

    new_data_copy = copy.deepcopy(data)
    new_data_copy.pop_last()

    check_always_needed(data)

    assert new_data_copy == old_data


def check_index_access(data, index, value):
    '''Check functionality of the [] access.'''
    old_data = copy.deepcopy(data)

    data[index] = value

    check_always_needed(data)

    assert data[index == value]
    assert data.size() == old_data.size

    for i in range(data.size()):
        if i != index:
            assert data[i] == old_data[i]


def check_pop_last(data):
    '''Check functionality of the pop_last() method.'''
    old_data = copy.deepcopy(data)

    if old_data.size() == 0:
        with pytest.raises(Exception):
            data.pop_last()
    else:
        data.pop_last()

        check_always_needed(data)

        assert data.size() == old_data.size() - 1

        for i in range(old_data.size() - 1):
            assert data[i] == old_data[i]


def check_pop_first(data):
    '''Check functionality of the pop_first() method.'''
    old_data = copy.deepcopy(data)

    if old_data.size() == 0:
        with pytest.raises(Exception):
            data.pop_first()
    else:
        data.pop_first()

        check_always_needed(data)

        assert data.size() == old_data.size() - 1

        for i in range(1, old_data.size()):
            assert data[i-1] == old_data[i]


def check_always_needed(data):
    '''Check the other axioms.'''
    if data.size() != 0:
        assert data.size() <= data.capacity()
        assert data.first() == data[0]
        assert data.last() == data[data.size() - 1]

#---------------------------------------------------------------------------------------------------------------------#

def measure_times():
    d = array_deque()
    d_slow = slow_array_deque()

    time_array = []
    slow_time_array = []

    n_range = list(range(513))

    for i in n_range:
        time_array.append(timeit.timeit("d.push(i)", globals=locals(), number=1))
        slow_time_array.append(timeit.timeit("d_slow.push(i)", globals=locals(), number=1))

    # Man sieht, dass die amortisierte Komplexität bei der ersten Versio konstant ist.
    # Bei der zweiten ist die Zeit jetzt ungefähr linear.

    plt.step(n_range, time_array, where='post')
    plt.xlabel("Number of current elements")
    plt.ylabel("Time to push new element")
    plt.grid(axis="x")
    plt.title("Times to push with variable number of current elements (fast Version)")
    plt.show()

    plt.step(n_range, slow_time_array, where='post')
    plt.xlabel("Number of current elements")
    plt.ylabel("Time to push new element")
    plt.grid(axis="x")
    plt.title("Times to push with variable number of current elements (slow Version)")
    plt.show()

#---------------------------------------------------------------------------------------------------------------------#

if __name__ == "__main__":

    measure_times()
    doctest.testmod()
