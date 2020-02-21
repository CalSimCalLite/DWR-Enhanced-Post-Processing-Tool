/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

package gov.ca.water.reportengine.executivereport;

import org.junit.jupiter.api.Test;

public class TestReservoirOperationsRow extends ExecutiveReportTestBase
{
    @Test
    void testResOpsRowWithBaseOnly() throws Exception
    {
        testBaseOnly( "NOD Reservoir Operation");
    }

    @Test
    void testResOpsRowWithOneAlternativeSameModel() throws Exception
    {
        testOneAlternativeSameModel( "NOD Reservoir Operation");
    }

}