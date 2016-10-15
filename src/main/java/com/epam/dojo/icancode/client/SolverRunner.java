package com.epam.dojo.icancode.client;

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


/**
 * Created by Oleksandr_Baglai on 2016-10-15.
 */
public class SolverRunner {

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args[0] == "java") {
            System.out.println("Running Java client");
            YourSolver.main(args);
        } else {
            System.out.println("Running Kotlin client");
            com.epam.dojo.icancode.client.YourSolverKt.main(args);
        }
    }
}
