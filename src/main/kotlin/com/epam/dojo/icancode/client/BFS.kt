package com.epam.dojo.icancode.client

import com.codenjoy.dojo.client.Direction
import com.codenjoy.dojo.services.Point
import com.codenjoy.dojo.services.PointImpl
import com.epam.dojo.icancode.model.Elements
import com.epam.dojo.icancode.model.Elements.Layers.*;
import com.epam.dojo.icancode.model.Elements.*;
import java.util.*

class Laser(val pos: Point, val direction: Direction) {
    var phase = -1;
    var period = -1;
    var curLasers = 0;

    fun isGood(time: Int, dist: Int): Boolean {
        if (dist<0) return true;
        if (period > 0 && (dist - (phase + time)) % period == 0) {
            return false;
        }
        if (dist < time || (curLasers and (1 shl (dist - time))) != 0) {
            return false;
        }
        return true;
    }

    var field = mutableListOf(0);

    fun fill(board: Board) {
        for (i in 0..board.size() - 1) {
            field.add(0);
        }
        var p = direction.change(pos);
        while (!board.isBarrierAt(p.x, p.y)) {
            field[p.x] = field[p.x] or (1 shl p.y);
            p = direction.change(p)
        }
    }

    fun dist(p: Point): Int {
        if ((field[p.x] and (1 shl p.y)) != 0) {
            return Math.abs(p.x - pos.x) + Math.abs(p.y - pos.y);
        }
        return -1;
    }

    fun dist(p: RoboState): Int {
        if ((field[p.x] and (1 shl p.y)) != 0) {
            return Math.abs(p.x - pos.x) + Math.abs(p.y - pos.y);
        }
        return -1;
    }
}

class PersistantData() {
    val lasers = mutableListOf<Laser>();

    fun test(board: Board) {
        for (i in 0..board.size() - 1)
            for (j in 0..board.size() - 1) {
                val e = board.getAt(LAYER1, i, j);
                if (isMachine(e)) {
                    var test = true;
                    for (laser in lasers) {
                        if (laser.pos.x == i && laser.pos.y == j) {
                            test = false;
                            break;
                        }
                    }
                    if (test) {
                        val laser = Laser(PointImpl(i, j), directionOfLaser(e));
                        laser.fill(board);
                        lasers.add(laser);
                    }
                }
            }

        for (laser in lasers) {
            //if (laser.period < INF) {
            //    laser.period = 7;
            //}
            if (laser.phase >= 0) {
                if (laser.period > 0) {
                    laser.phase = (laser.phase + 1) % laser.period;
                } else {
                    laser.phase++;
                }
            }
            val elem = board.getAt(LAYER1, laser.pos.x, laser.pos.y);
            if (isMachine(elem)) {
                laser.curLasers = if (isMachineReady(elem)) 1 else 0;
                if (laser.curLasers > 0) {
                    if (laser.period != INF) {
                        if (laser.period < 0) {
                            if (laser.phase >= 0) {
                                laser.period = Math.abs(laser.phase);
                            }
                        } else
                            if (laser.phase % laser.period != 0) {
                                laser.period = INF;
                                System.out.println("BUG PERIOD LASER");
                            }
                    }
                    laser.phase = 0;
                    laser.curLasers = 1;
                }
                var p = laser.direction.change(laser.pos);
                var t = 1;
                do {
                    if (isLaserDir(board.getAt(LAYER2, p.x, p.y), laser.direction)) {
                        if (laser.period != INF) {
                            if (laser.period < 0) {
                                if (laser.phase != t) {
                                    laser.period = Math.abs(laser.phase - t);
                                }
                            } else
                                if ((laser.phase - t) % laser.period != 0) {
                                    laser.period = INF;
                                    System.out.println("BUG PERIOD LASER");
                                }
                        }
                        laser.phase = t;
                        laser.curLasers = laser.curLasers or (1 shl t);
                    }
                    p = laser.direction.change(p);
                    t++;
                } while (board.isBarrierAt(p.x, p.y));
            }
        }
    }

    fun isGood(rs: RoboState): Boolean {
        for (laser in lasers) {
            val d = laser.dist(rs);
            if (!laser.isGood(rs.time, d)) {
                return false;
            }
        }
        return true;
    }

    companion object {
        val INF = 100000;

        fun isMachine(e: Elements): Boolean {
            return e == LASER_MACHINE_READY_DOWN ||
                    e == LASER_MACHINE_READY_UP ||
                    e == LASER_MACHINE_READY_RIGHT ||
                    e == LASER_MACHINE_READY_LEFT ||
                    e == LASER_MACHINE_CHARGING_DOWN ||
                    e == LASER_MACHINE_CHARGING_UP ||
                    e == LASER_MACHINE_CHARGING_RIGHT ||
                    e == LASER_MACHINE_CHARGING_LEFT;
        }

        fun isMachineReady(e: Elements): Boolean {
            return e == LASER_MACHINE_READY_DOWN ||
                    e == LASER_MACHINE_READY_UP ||
                    e == LASER_MACHINE_READY_RIGHT ||
                    e == LASER_MACHINE_READY_LEFT;
        }

        fun isLaserDir(e: Elements, direction: Direction): Boolean {
            return e == LASER_DOWN && direction == Direction.DOWN ||
                    e == LASER_LEFT && direction == Direction.LEFT ||
                    e == LASER_UP && direction == Direction.UP ||
                    e == LASER_RIGHT && direction == Direction.RIGHT;
        }

        fun directionOfLaser(e: Elements): Direction {
            if (e == LASER_MACHINE_READY_DOWN ||
                    e == LASER_MACHINE_CHARGING_DOWN ||
                    e == LASER_DOWN) return Direction.DOWN;
            if (e == LASER_MACHINE_READY_UP ||
                    e == LASER_MACHINE_CHARGING_UP ||
                    e == LASER_UP) return Direction.UP;
            if (e == LASER_MACHINE_READY_RIGHT ||
                    e == LASER_MACHINE_CHARGING_RIGHT ||
                    e == LASER_RIGHT) return Direction.RIGHT;
            if (e == LASER_MACHINE_READY_LEFT ||
                    e == LASER_MACHINE_CHARGING_LEFT ||
                    e == LASER_LEFT) return Direction.LEFT;
            return Direction.ACT;
        }
    }
}

class BFS(val pd: PersistantData, val board: Board, val MAXTIME: Int) {

    val targetField = Array(board.size(), { it -> Array<Int>(board.size(), { stuff -> 0 }) })

    fun bfsAll(p: Point, maxTime: Int,
               targets: List<Point>): List<RoboState> {
        Arrays.fill(was, 0);
        for (i in 0..board.size() - 1) {
            for (j in 0..board.size() - 1) {
                targetField[i][j] = -1;
            }
        }
        for (i in 0..targets.size - 1) {
            val p = targets[i];
            if (i != 0) {
                targetField[p.x][p.y] = (i - 1) % (MAX_TARGET - 1) + 1;
            } else {
                targetField[p.x][p.y] = 0;
            }
        }

        val init = RoboState(board, MAXTIME);
        with(init) {
            x = p.x;
            y = p.y;
            scoreMask = 0;
            isFlying = 0;
            time = 0;
            prev = null;
        }
        var cur = mutableListOf(init);
        var next = mutableListOf<RoboState>();
        var temp = mutableListOf<RoboState>();
        var ans = mutableListOf<RoboState>();

        for (t in 0..maxTime) {
            for (rs in cur) {
                goNext(rs, next, ans);
            }

            temp = next;
            next = cur;
            cur = temp;
            next.clear();
        }

        return ans;
    }

    val temp = RoboState(board, MAXTIME);

    fun goNext(cur: RoboState, next: MutableList<RoboState>, ans: MutableList<RoboState>) {
        for (doJump in 0..1) {
            for (dir in dirs) {
                if (doJump > 0 && board.isWallAt(dir.changeX(cur.x), dir.changeY(cur.y))) {
                    continue;
                }
                temp.copy(cur);
                temp.doMove(dir, doJump > 0);
                if (!board.isGoodAt(temp.x, temp.y)) {
                    continue;
                }
                if (temp.isFlying == 0 && !pd.isGood(temp)) {
                    continue;
                }
                temp.time++;
                if (temp.isFlying == 0 && !pd.isGood(temp)) {
                    continue;
                }
                if (targetField[temp.x][temp.y] >= 0) {
                    temp.scoreMask = temp.scoreMask or (1 shl targetField[temp.x][temp.y]);
                }

                val h = temp.hashCode();
                if ((was[h / 32] and (1 shl (h % 32))) != 0) {
                    continue;
                }
                //TODO: look at 7 turns back, time optimization

                was[h / 32] = was[h / 32] or (1 shl (h % 32));

                val newRobo = RoboState(board, MAXTIME);
                newRobo.copy(temp);
                newRobo.moveDir = dir;
                newRobo.prev = cur;

                if (board.getAt(LAYER1, newRobo.x, newRobo.y) == Elements.EXIT) {
                    ans.add(newRobo);
                    continue;
                }

                next.add(newRobo);
            }
        }
    }


    companion object {
        val MAX_TARGET = 10;
        val was = Array<Int>(20 * 20 * 2 * 256 * (1 shl MAX_TARGET) / 32, { it -> 0 });
        val dirs = arrayOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.STOP);
    }
}
