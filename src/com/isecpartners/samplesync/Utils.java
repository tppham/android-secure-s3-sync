package com.isecpartners.samplesync;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A collection of utilities.
 */
public class Utils {
    static class Chunk {
        int count;
        byte [] data;

        Chunk(int c, byte [] d) {
            count = c;
            data = d;
        }
    }

    /**
     * With this code I have defiled my family's honor. My shame is eternal and
     * incalculable.
     *
     * @param stream An input stream to read all the bytes from.
     *
     * @return All the bytes read from the input stream.
     */
    public static byte [] readAll(InputStream stream) throws IOException {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        while (true) {
            byte [] d = new byte [4096];
            int c = stream.read(d);

            if (-1 == c)
                break;

            chunks.add(new Chunk(c, d));
        }

        int ttl = 0;
        for(Chunk c : chunks)
            ttl += c.count;

        byte [] d = new byte [ttl];
        int offst = 0;
        for(Chunk c : chunks) {
            System.arraycopy(c.data, 0, d, offst, c.count);
            offst += c.count;
        }
        return d;
    }
}

