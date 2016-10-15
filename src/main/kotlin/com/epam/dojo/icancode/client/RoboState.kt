package com.epam.dojo.icancode.client

import com.epam.dojo.icancode.model.Elements
import com.codenjoy.dojo.client.Direction

class RoboState(val board: Board, val maxTime: Int) {
    var x: Int = 0;
    var y: Int = 0;
    var time: Int = 0;
    var isFlying: Int = 0;
    var scoreMask: Int = 0;
    var prev: RoboState? = null;
    var moveDir: Direction = Direction.DOWN;

    override fun hashCode(): Int {
        var p = board.size();
        var h = x;
        h += p * y; p *= board.size();
        h += p * isFlying; p *= 2;
        h += p * time; p *= maxTime;
        h += p * scoreMask;
        return h;
    }

    fun setHash(h_: Int) {
        var h = h_;
        x = h % board.size();h /= board.size();
        y = h % board.size();h /= board.size();
        isFlying = h % 2;h /= 2;
        time = h % maxTime; h /= maxTime;
        scoreMask = h;
    }

    fun copy(r: RoboState) {
        x = r.x;
        y = r.y;
        time = r.time;
        isFlying = r.isFlying;
        scoreMask = r.scoreMask;
    }

    fun doMove(dir: Direction, jump: Boolean) {
        if (isFlying > 0) {
            isFlying--;
            return;
        }

        moveDir = dir;
        x = dir.changeX(x);
        y = dir.changeY(y);
        if (jump) {
            x = dir.changeX(x);
            y = dir.changeY(y);
            isFlying++;
        }
    }
}
