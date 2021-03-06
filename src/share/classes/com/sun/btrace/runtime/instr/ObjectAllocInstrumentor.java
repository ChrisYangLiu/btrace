/*
 * Copyright (c) 2008, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the Classpath exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.btrace.runtime.instr;

import com.sun.btrace.runtime.Constants;
import static com.sun.btrace.org.objectweb.asm.Opcodes.*;
import com.sun.btrace.util.LocalVariableHelper;

/**
 * This visitor helps in inserting code whenever an object
 * is allocated. The code to insert on object alloc may be
 * decided by  derived class. By default, this class inserts
 * code to print a message.
 *
 * @author A. Sundararajan
 */
public class ObjectAllocInstrumentor extends MethodInstrumentor {
    private final boolean needsInitialization;
    private boolean instanceCreated = false;

    public ObjectAllocInstrumentor(LocalVariableHelper mv, String parentClz, String superClz,
        int access, String name, String desc) {
        this(mv, parentClz, superClz, access, name, desc, false);
    }

    public ObjectAllocInstrumentor(LocalVariableHelper mv, String parentClz, String superClz,
        int access, String name, String desc, boolean needsInitialization) {
        super(mv, parentClz, superClz, access, name, desc);
        this.needsInitialization = needsInitialization;
    }

    @Override
    public void visitTypeInsn(int opcode, String desc) {
        if (opcode == NEW) {
            beforeObjectNew(desc);
        }
        super.visitTypeInsn(opcode, desc);
        if (opcode == NEW) {
            if (needsInitialization) {
                instanceCreated = true;
            } else {
                afterObjectNew(desc);
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean iface) {
        super.visitMethodInsn(opcode, owner, name, desc, iface);
        if (instanceCreated) {
            if (Constants.CONSTRUCTOR.equals(name)) {
                instanceCreated = false;
                afterObjectNew(owner);
            }
        }
    }

    protected void beforeObjectNew(String desc) {}

    protected void afterObjectNew(String desc) {}
}