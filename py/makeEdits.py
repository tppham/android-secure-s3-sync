#!/usr/bin/env python

from marsh import *

def main() :
    cs = load("orig/remote.bin")
    print 'original:'
    print cs
    print

    # contact deletion
    print 'deleting %s' % cs.contacts[1]
    del cs.contacts[1]

    # contact insertion
    d = Data(Data.PHONE, '000-000-6111', 1, None)
    c = Contact(d)
    print 'adding %s' % (c)
    cs.contacts.append(c)

    # data deletion
    print 'deleting %s from %s' % (cs.contacts[0].data[1], cs.contacts[0])
    del cs.contacts[0].data[1]

    # data insertion
    d = Data(Data.PHONE, '000-000-4111', 1, None)
    print 'adding %s to %s' % (d, cs.contacts[0])
    cs.contacts[0].data.append(d)

    print
    print
    print cs
    save("remote2.bin", cs)

if __name__ == '__main__' :
    main()


