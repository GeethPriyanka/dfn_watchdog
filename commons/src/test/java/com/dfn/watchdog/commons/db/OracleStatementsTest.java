package com.dfn.watchdog.commons.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class OracleStatementsTest {
    private Connection connection = Mockito.mock(Connection.class);
    private PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ;
    private OracleStatements oracleStatements;

    @Before
    public void init() throws Exception {
        Mockito.doReturn(preparedStatement).when(connection).prepareStatement(Mockito.anyString());
        Mockito.doNothing().when(preparedStatement).setLong(Mockito.anyInt(), Mockito.anyLong());
        Mockito.doNothing().when(preparedStatement).setInt(Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(preparedStatement).setString(Mockito.anyInt(), Mockito.anyString());
        oracleStatements = new OracleStatements(connection);
    }

    @Test
    public void testInsertClient() throws Exception {
        PreparedStatement ps = oracleStatements.insertClient(123L, (short) 1);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testUpdateClient() throws Exception {
        PreparedStatement ps = oracleStatements.updateClient(123L, (short) 1);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testGetClientRoute() throws Exception {
        PreparedStatement ps = oracleStatements.getClientRoute(123L);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testGetClientsFromRoute() throws Exception {
        PreparedStatement ps = oracleStatements.getClientsFromRoute((short) 1);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testUpdateRoutes() throws Exception {
        PreparedStatement ps = oracleStatements.updateRoutes((short) 1, (short) 2);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testInsertClientHistory() throws Exception {
        PreparedStatement ps = oracleStatements.insertClientHistory(123L, (short) 1);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testBulkInsertHistory() throws Exception {
        PreparedStatement ps = oracleStatements.bulkInsertHistory(new ArrayList<>(), (short) 1);
        Assert.assertNotNull(ps);
    }

    @Test
    public void testGetAllClientRoutes() throws Exception {
        PreparedStatement ps = oracleStatements.getAllClientRoutes();
        Assert.assertNotNull(ps);
    }

    @Test
    public void testGetClientRouteHistory() throws Exception {
        PreparedStatement ps = oracleStatements.getClientRouteHistory(123L);
        Assert.assertNotNull(ps);
    }
}
