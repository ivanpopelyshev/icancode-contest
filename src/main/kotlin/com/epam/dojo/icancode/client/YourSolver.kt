package com.epam.dojo.icancode.client

/*-
 * #%L
 * iCanCode - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 EPAM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.client.Direction
import com.codenjoy.dojo.client.Direction.*
import com.codenjoy.dojo.client.WebSocketRunner
import com.codenjoy.dojo.services.Dice
import com.codenjoy.dojo.services.PointImpl
import com.codenjoy.dojo.services.RandomDice
import com.epam.dojo.icancode.model.Elements.*;
import com.epam.dojo.icancode.model.Elements.Layers.LAYER1
import com.epam.dojo.icancode.model.Elements.Layers.LAYER2

/**
 * Your AI
 */
class YourKotlinSolver() : AbstractSolver() {
    val data = PersistantData();
    var curMoney: Double = 0.0;
    var prev = PointImpl(10, 10);

    override fun whatToDo(board: Board): Command {
        with(board) {
            data.test(board);
            if (!board.isMeAlive) return Command.doNothing()

            var goals = board.gold
            if (goals.isEmpty()) {
                goals = board.exits
            }
            val p = board.me ?: prev;
            if (!board.isAt(LAYER2, p.x, p.y, ROBO)) {
                return Command.doNothing();
            }

            val bfs = BFS(data, board, 35);
            val stuff = bfs.bfsAll(p, 30, goals);

            var best: RoboState? = null;
            var bestScore = -1.0;

            for (s in stuff) {
                var score: Double = curMoney;
                for (i in 0..goals.size - 1) {
                    if ((s.scoreMask and (1 shl i)) != 0) {
                        score += 1;
                    }
                }
                score /= (10.0 + s.time);
                if (bestScore < score) {
                    bestScore = score;
                    best = s;
                }
            }

            while (best?.prev?.prev != null) {
                best = best?.prev;
            }
            val b = best;
            b ?: return Command.go(BFS.dirs[Math.floor(Math.random() * 5).toInt()]);
            val dir = b.moveDir;
            if (board.getAt(LAYER1, b.x, b.y) == GOLD) {
                curMoney++;
            } else if (board.getAt(LAYER1, b.x, b.y) == EXIT) {
                curMoney = 0.0;
            }
            prev = PointImpl(b.x, b.y);
            System.out.println("bestScore=$bestScore curMoney=$curMoney");

            if (dir != Direction.STOP) {
                if (b.isFlying > 0) {
                    return Command.jumpTo(dir);
                } else {
                    return Command.go(dir);
                }
            } else {
                if (b.isFlying > 0) {
                    return Command.jump();
                } else {
                    return Command.doNothing();
                }
            }
        }
    }
}

/**
 * Run this method for connect to the server and start the game
 */
fun main(args: Array<String>) {
    AbstractSolver.start("ivan.popelyshev@gmail.com", "192.168.1.5:8080", YourKotlinSolver())
}
