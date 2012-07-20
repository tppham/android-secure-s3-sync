#!/usr/bin/env python
"""
Data marshalling for the contact set format used by the synch store.

Requires https://github.com/mikeboers/PyTomCrypt
  (pip install PyTomCrypt or easy_install PyTomCrypt)
"""
import sys
from tomcrypt import cipher
import pbkdf2
#import debug

class Error(Exception) :
    pass

class ContactSet(object) :
    VERSION = 1
    def __init__(self, *args) :
        self.version = self.VERSION
        self.id = 0
        self.contacts = list(args)

    def __str__(self) :
        cs = '\n  '.join(str(c) for c in self.contacts)
        return '[ContactSet VER=%d ID=%d:\n  %s]' % (self.version, self.id, cs)

class Contact(object) :
    def __init__(self, *args) :
        self.acctType = None
        self.acctName = None
        self.locid = -1
        self.remid = -1
        self.data = list(args)
    def __str__(self) :
        ds = '\n    '.join(str(d) for d in self.data)
        return '[Contact: locid=%x remid=%x acct=%s/%s\n    %s]' % (self.locid, self.remid, self.acctType, self.acctName, ds)

class Data(object) :
    PHONE, EMAIL, NAME = 1,2,3
    def __init__(self, *args) :
        if args :
            self.kind = args[0]
            self.fields = args[1:]
        else :
            self.kind = 0
            self.fields = ()
    def __str__(self) :
        fs = ', '.join(repr(f) for f in self.fields)
        return '[Data: %s]' % (fs)

    
# note: this is a test program that does not care about
# the security of your data!  Do not use this for anything
# sensitive!

def genSalt() : # we dont care about security here
    return "B" * 8
def genIV() : # we don't care about security here
    return "A" * 16

def genKey(pw, salt, niters) :
    return pbkdf2.PBKDF2(pw, salt, niters).read(32)

def encrypt(pw, iv, buf) :
    return cipher.Cipher(key=pw, iv=iv, cipher='aes', mode='eax').encrypt(buf)
def decrypt(pw, iv, buf) :
    return cipher.Cipher(key=pw, iv=iv, cipher='aes', mode='eax').decrypt(buf)

class Blob(object) :
    MAGIC = 0x1badd00d
    def __init__(self, pw, salt, iv, niters=1000) :
        self.pw = pw
        self.salt = salt
        self.iv = iv
        self.niters = niters
        self.magic = self.MAGIC
        self.cset = ContactSet()
    def __str__(self) :
        return '[Blob salt=%s niters=%d magic=%x cset=%s]' % (self.salt.encode('hex'), self.niters, self.magic, self.cset)

class Buf(object) :
    def __init__(self, b=None) :
        if b is None :
            self.b = []
        else :
            self.b = map(ord, b)
        self.pos = 0

    def __str__(self) :
        return ''.join(chr(x) for x in self.b)

    def remaining(self) :
        return len(self.b) - self.pos
    def getEof(self) :
        resid = self.remaining()
        if resid :
            raise Error("%d extra bytes found at %d!" % (self.pos, resid))
    def get8(self) :
        if self.pos >= len(self.b) :
            raise Error("out of data at %d!" % (self.pos))
        x = self.b[self.pos]
        self.pos += 1
        return x
    def put8(self, x) :
        self.b.append(x & 0xff)
        self.pos += 1
        return self
    def get16(self) :
        return self.get8() << 8 | self.get8()
    def put16(self, x) :
        return self.put8(x >> 8).put8(x)
    def get32(self) :
        return self.get16() << 16 | self.get16()
    def put32(self, x) :
        return self.put16(x >> 16).put16(x)
    def get64(self) :
        return self.get32() << 32 | self.get32()
    def put64(self, x) :
        return self.put32(x >> 32).put32(x)
    def getBytes(self, n) :
        return ''.join(chr(self.get8()) for m in xrange(n))
    def putBytes(self, x) :
        for ch in x :
            self.put8(ord(ch))
        return self
    def getStr(self) :
        n = self.get16()
        if n != 0xffff :
            return self.getBytes(n).decode('utf8')
    def putStr(self, x) :
        if x is not None :
            self.put16(len(x))
            self.putBytes(x.encode('utf8'))
        else :
            self.put16(0xffff)
        return self

    def getContactSet(self) :
        c = ContactSet()
        c.version = self.get32()
        if c.version != 1 :
            raise Error("bad version %d at pos %d" % (c.version, self.pos))
        c.id = self.get32()
        cnt = self.get16()
        for n in xrange(cnt) :
            c.contacts.append(self.getContact())
        return c
    def putContactSet(self, c) :
        if c.version != 1 :
            raise Error("bad version: " + c.version)
        self.put32(c.version)
        self.put32(c.id)
        self.put16(len(c.contacts))
        for c in c.contacts :
            self.putContact(c)
        return self

    def getContact(self) :
        c = Contact()
        c.acctType = self.getStr()
        c.acctName = self.getStr()
        c.locid = self.get64()
        c.remid = self.get64()
        cnt = self.get16()
        for n in xrange(cnt) :
            c.data.append(self.getData())
        return c
    def putContact(self, c) :
        self.putStr(c.acctType)
        self.putStr(c.acctName)
        self.put64(c.locid)
        self.put64(c.remid)
        self.put16(len(c.data))
        for d in c.data :
            self.putData(d)
        return self

    def getData(self) :
        d = Data()
        d.kind = self.get8()
        if d.kind in (d.NAME,) :
            d.fields = self.getStr(), self.getStr(), self.getStr(), self.getStr(), self.getStr(), self.getStr(), self.getStr(), self.getStr(), self.getStr()
        elif d.kind in (d.PHONE, d.EMAIL) :
            d.fields = self.getStr(), self.get32(), self.getStr()
        else :
            raise Error("bad kind %d at pos %d!" % (d.kind, self.pos))
        return d
    def putData(self, d) :
        self.put8(d.kind)
        if d.kind in (d.NAME,) :
            self.putStr(d.fields[0])
            self.putStr(d.fields[1])
            self.putStr(d.fields[2])
            self.putStr(d.fields[3])
            self.putStr(d.fields[4])
            self.putStr(d.fields[5])
            self.putStr(d.fields[6])
            self.putStr(d.fields[7])
            self.putStr(d.fields[8])
        elif d.kind in (d.PHONE, d.EMAIL) :
            self.putStr(d.fields[0])
            self.put32(d.fields[1])
            self.putStr(d.fields[2])
        else :
            raise Error("bad kind %d!" % d.kind)
        return self

    def getBlob(self, pw) :
        salt = self.getBytes(8)
        niters = self.get32()
        iv = self.getBytes(16)
        x = Blob(pw, salt, iv, niters)

        key = genKey(x.pw, x.salt, x.niters)
        plain = decrypt(key, x.iv, self.getBytes(self.remaining()))
        buf = Buf(plain)

        x.magic = buf.get32()
        if x.magic != x.MAGIC :
            raise Error("bad magic number: %x" % x.magic)
        x.cset = buf.getContactSet()
        self.getEof()
        return x
    def putBlob(self, x) :
        self.putBytes(x.salt)
        self.put32(x.niters)
        self.putBytes(x.iv)

        buf = Buf()
        buf.put32(x.magic)
        buf.putContactSet(x.cset)
        key = genKey(x.pw, x.salt, x.niters)
        self.putBytes(encrypt(key, x.iv, str(buf)))

pw = 'the quick brown fox'
def load(fn) :
    d = file(fn, 'rb').read()
    b = Buf(d)
    blob = b.getBlob(pw)
    cs = blob.cset
    b.getEof()
    return cs

def save(fn, cs) :
    b = Buf()
    blob = Blob(pw, genSalt(), genIV())
    blob.cset = cs

    b.putBlob(blob)
    file(fn, 'wb').write(str(b))

def test() :
    print "last"
    print load('last.bin')
    print
    print "remote"
    r = load('remote.bin')
    print r

    # cycle it through the marshaller once
    print
    blob = Blob(pw, genSalt(), genIV())
    blob.cset = r
    s = str(Buf().putBlob())
    print 'hex', s.encode('hex')
    b = Buf(s)
    r2 = b.getBlob(pw)
    b.getEof()
    print 'remote after remarshall', r2

def view(fn) :
    print 'loading', fn
    print load(fn)
    
if __name__ == '__main__' :
    #test()
    for fn in sys.argv[1:] :
        view(fn)
