package cn.xydzjnq.tetris.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockFatory {
    public static Block createBlock() {
        List<Block> blockList = new ArrayList<>();
        blockList.add(new IBlock());
        blockList.add(new JBlock());
        blockList.add(new LBlock());
        blockList.add(new OBlock());
        blockList.add(new SBlock());
        blockList.add(new TBlock());
        blockList.add(new ZBlock());
        Random random = new Random();
        int randomInt = random.nextInt(6);
        return blockList.get(randomInt);
    }
}
