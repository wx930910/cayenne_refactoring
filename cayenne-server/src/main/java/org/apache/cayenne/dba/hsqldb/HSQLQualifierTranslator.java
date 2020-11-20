/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.dba.hsqldb;

import java.io.IOException;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.access.translator.select.QueryAssembler;
import org.apache.cayenne.access.translator.select.TrimmingQualifierTranslator;
import org.apache.cayenne.exp.parser.ASTExtract;
import org.apache.cayenne.exp.parser.ASTFunctionCall;
import org.apache.cayenne.exp.parser.PatternMatchNode;

/**
 * @since 4.0
 */
public class HSQLQualifierTranslator extends TrimmingQualifierTranslator {

    public HSQLQualifierTranslator(QueryAssembler queryAssembler) {
        super(queryAssembler, HSQLDBAdapter.TRIM_FUNCTION);
    }

    @Override
    protected void appendLikeEscapeCharacter(PatternMatchNode patternMatchNode)
            throws IOException {

        char escapeChar = patternMatchNode.getEscapeChar();

        if ('?' == escapeChar) {
            throw new CayenneRuntimeException("the escape character of '?' is illegal for LIKE clauses.");
        }

        if (0 != escapeChar) {
            // this is a difference with super implementation - HSQL driver seems does not
            // support JDBC escape syntax, so creating an explicit SQL escape:
            out.append(" ESCAPE '");
            out.append(escapeChar);
            out.append("'");
        }
    }

    @Override
    protected void appendFunction(ASTFunctionCall functionExpression) {
        // from documentation:
        // CURRENT_TIME returns a value of TIME WITH TIME ZONE type.
        // LOCALTIME returns a value of TIME type.
        // CURTIME() is a synonym for LOCALTIME.
        // use LOCALTIME to better align with other DBs
        if("CURRENT_TIME".equals(functionExpression.getFunctionName())) {
            out.append("LOCALTIME");
        } else {
            super.appendFunction(functionExpression);
        }
    }

    @Override
    protected void appendExtractFunction(ASTExtract functionExpression) {
        switch (functionExpression.getPart()) {
            case DAY_OF_WEEK:
            case DAY_OF_MONTH:
            case DAY_OF_YEAR:
                // hsqldb variants are without '_'
                out.append(functionExpression.getPart().name().replace("_", ""));
                break;
            default:
                appendFunction(functionExpression);
        }
    }
}
