package com.davidaq.logio.util;

import java.util.HashMap;
import java.util.LinkedList;

public class IntegerCombineQueue implements Queue<Long, IntegerCombineQueue.Block> {
    public static class Block {
        public long start;
        public long length;
        boolean disposed = false;
    }

    public static class BlockRef {
        public Block value;
    }

    private final HashMap<Long, BlockRef> blockMap = new HashMap<>();
    private final LinkedList<Block> queue = new LinkedList<>();

    @Override
    public void push(Long val) {
        synchronized (this) {
            if (blockMap.containsKey(val)) {
                return;
            }
            BlockRef ref = new BlockRef();
            blockMap.put(val, ref);
            BlockRef existing = blockMap.get(val - 1);
            if (existing != null) {
                ref.value = existing.value;
                ref.value.length++;
            } else {
                ref.value = new Block();
                ref.value.start = val;
                ref.value.length = 1;
                queue.addLast(ref.value);
            }
            existing = blockMap.get(val + 1);
            if (existing != null) {
                existing.value.disposed = true;
                ref.value.length += existing.value.length;
                existing.value = ref.value;
            }
            this.notify();
        }
    }

    @Override
    public Block shift() {
        synchronized (this) {
            while (!queue.isEmpty()) {
                Block dequeue = queue.removeFirst();
                if (dequeue.disposed) {
                    continue;
                }
                for (int i = 0; i < dequeue.length; i++) {
                    blockMap.remove(i + dequeue.start);
                }
                return dequeue;
            }
            return null;
        }
    }
}
