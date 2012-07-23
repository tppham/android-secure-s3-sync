#!/usr/bin/env python
import re, os, sys
#import debug

def splitAttrs(s) :
    last = 0
    name,rest = s.split('=', 1)
    for x in re.finditer(', [a-z_0-9]*=', rest) :
        val = rest[last : x.start()]
        yield name, val
        name = rest[x.start() + 2 : x.end()-1]
        last = x.end() 
    val = rest[last:]
    yield name,val

def read(f) :
    db = {'data': {}, 'contact': {}}

    ls = [l.strip().split(': ',1)[1] for l in f]
    for l in ls :
        if ':' in l :
            ty,rest = l.split(': ', 1)
            if ty not in db.keys() :
                continue
            attrs = dict(splitAttrs(rest))
            
            db[ty][attrs['_id']] = attrs
    return db
    
if len(sys.argv) > 1 :
    f = file(sys.argv[1], 'r')
else :
    f = os.popen('adb logcat -ds Sync', 'r')
db = read(f)
for c in db['contact'].values() :
    cid = c['_id']
    deleted = ('','deleted')[int(c['deleted'])]
    print 'contact', cid, c['account_type'], c['sync1'], deleted, c['contact_id']
    for d in db['data'].values() :
        if d['raw_contact_id'] == cid :
            did = d['_id']
            print ' ', 'data', did, d['data1'], d['data2'], d['data_sync1']
    print
