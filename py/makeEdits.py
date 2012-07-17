#!/usr/bin/env python
"""
Load in the "remote" data, make edits to it, and save it.
"""

from marsh import *
import s3Dat

def main(useFile=False) :
    if useFile :
        cs = load("orig/remote.bin")
    else :
        cs = s3Dat.load()
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
    if useFile :
        save("remote2.bin", cs)
    else :
        s3Dat.save(cs)

if __name__ == '__main__' :
    main()

