package org.trebor.splink;

import static java.lang.String.format;
import static org.trebor.splink.MessageHandler.Type.SPLASH;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.openrdf.model.Statement;
import org.openrdf.query.Binding;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class DefaultResultsListener implements ResultsListener
{
  private Splink mSplink;
  private JTable mResultTable;
  TableCellRenderer mTableHeaderRenderer;
  
  public DefaultResultsListener(Splink splink, JTable resultTable, TableCellRenderer tableHeaderRenderer)
  {
    mSplink = splink;
    mResultTable = resultTable;
    mTableHeaderRenderer = tableHeaderRenderer;
  }

  public int onTuple(TupleQueryResult result)
    throws QueryEvaluationException
  {
    // create the table model

    @SuppressWarnings("serial")
    DefaultTableModel tm = new DefaultTableModel()
    {
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };

    // add columnds to table

    Map<String, Integer> columnMap = new HashMap<String, Integer>();
    for (String binding : result.getBindingNames())
    {
      columnMap.put(binding, tm.getColumnCount());
      tm.addColumn(binding);
    }

    // populate the table

    while (result.hasNext())
    {
      String[] row = new String[columnMap.size()];
      Iterator<Binding> rowData = result.next().iterator();
      while (rowData.hasNext())
      {
        Binding rowBinding = rowData.next();
        String binding = rowBinding.getName();
        String uri = rowBinding.getValue().toString();
        if (!mSplink.showLongUri())
          uri = mSplink.shortUri(uri);
        row[columnMap.get(binding)] = uri;
      }

      tm.addRow(row);
    }

    // update the display

    mResultTable.setModel(tm);
    TableColumnModel columnModel = mResultTable.getColumnModel();
    for (int i = 0; i < tm.getColumnCount(); ++i)
      columnModel.getColumn(i).setHeaderRenderer(mTableHeaderRenderer);
    mSplink.setResultComponent(mResultTable);

    // return row count

    return tm.getRowCount();
  }

  public int onGraph(GraphQueryResult result)
    throws QueryEvaluationException
  {
    // create the table model

    @SuppressWarnings("serial")
    DefaultTableModel tm = new DefaultTableModel()
    {
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };

    // add columns

    for (String name : new String[]
    {
      "subject", "predicate", "object"
    })
      tm.addColumn(name);

    // populate the table

    while (result.hasNext())
    {
      Vector<String> row = new Vector<String>();
      Statement rowData = result.next();
      if (mSplink.showLongUri())
      {
        row.add(rowData.getSubject().toString());
        row.add(rowData.getPredicate().toString());
        row.add(rowData.getObject().toString());
      }
      else
      {
        row.add(mSplink.shortUri(rowData.getSubject().toString()));
        row.add(mSplink.shortUri(rowData.getPredicate().toString()));
        row.add(mSplink.shortUri(rowData.getObject().toString()));
      }

      tm.addRow(row);
    }

    // update the display

    mResultTable.setModel(tm);
    for (int i = 0; i < tm.getColumnCount(); ++i)
      mResultTable.getColumnModel().getColumn(i)
        .setHeaderRenderer(mTableHeaderRenderer);
    mSplink.setResultComponent(mResultTable);

    // return row count

    return tm.getRowCount();
  }

  public boolean onBoolean(boolean result)
  {
    mSplink.handleMessage(SPLASH, format("%b", result).toUpperCase());
    return result;
  }

  public void onUpdate()
  {
    mSplink.handleMessage(SPLASH, "update performed");
  }
}