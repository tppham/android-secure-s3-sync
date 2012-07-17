#!/usr/bin/env python

import sys
import S3
import marsh

def getS3() :
    sec = [l.strip() for l in file('../secrets.txt', 'r')]
    return S3.AWSAuthConnection(sec[0], sec[1])

def createBucket(s3, n) :
    s3.create_bucket(n)

def listBuckets(s3) :
    for b in s3.list_all_my_buckets().entries :
        print b.name

def put(s3, n, k, v) :
    x = s3.put(n, k, S3.S3Object(v), { 'Content-Type': 'text/plain' })
    return x.message

def get(s3, n, k) :
    return s3.get(n, k).object.data

defBucket = 'mycontacts'
defKey = 'synch'
def load(bucket=defBucket, key=defKey) :
    d = get(getS3(), bucket, key)
    b = marsh.Buf(d)
    cs = b.getContactSet()
    b.getEof()
    return cs

def save(cs, bucket=defBucket, key=defKey) :
    d = get(getS3(), bucket, key)
    b = marsh.Buf()
    b.putContactSet(cs)
    return put(getS3(), bucket, key, str(b))

def saveRaw(fn, bucket=defBucket, key=defKey) :
    d = get(getS3(), bucket, key)
    print 'writing to', fn
    file(fn, 'wb').write(d)

def clear(bucket=defBucket, key=defKey) :
    #getS3().delete(bucket, key)
    getS3().delete_bucket(bucket)

def dump() :
    cs = load()
    print cs

if __name__ == '__main__' :
    if len(sys.argv) > 1 :
        if sys.argv[1] == 'clear' :
            print 'clearing'
            clear()
        else :
            print 'saving', sys.argv[1]
            saveRaw(sys.argv[1])
    else :
        print 'dumping'
        dump()

