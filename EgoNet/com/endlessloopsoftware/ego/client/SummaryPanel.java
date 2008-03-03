package com.endlessloopsoftware.ego.client;

/****
 * 
 * Copyright (c) 2007, Endless Loop Software, Inc.
 * 
 *  This file is part of EgoNet.
 *
 *    EgoNet is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    EgoNet is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;

import com.endlessloopsoftware.ego.Shared;
import com.endlessloopsoftware.ego.client.statistics.StatisticsArrayPanel;
import com.endlessloopsoftware.ego.client.statistics.models.StatTableModel;
import com.endlessloopsoftware.elsutils.SwingWorker;
import com.endlessloopsoftware.elsutils.files.DirList;
import com.endlessloopsoftware.elsutils.files.FileHelpers;

import electric.xml.Document;
import electric.xml.Element;

public class SummaryPanel
   extends JPanel
{
   private final JButton _finishedButton = new JButton("Finished");
   private JPanel        _summaryPanel;

   private StatRecord[]  _stats          = new StatRecord[0];
   private int           _recordCount    = 0;

   public SummaryPanel(StatRecord[] stats)
   {
      setLayout(new GridBagLayout());

      /* Get data to display */
      _stats = stats;
      _recordCount = stats.length;

      /* Load table */
      _summaryPanel = new StatisticsArrayPanel(new SummaryModel(this));

      add(_summaryPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      add(_finishedButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                                  GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

      _finishedButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            finishedButton_actionPerformed(e);
         }
      });
   }

   public SummaryPanel(ProgressMonitor progress)
   {
      setLayout(new GridBagLayout());

      /* Get data to display */
      loadInterviewArray(progress);

      /* Load table */
      _summaryPanel = new StatisticsArrayPanel(new SummaryModel(this));

      add(_summaryPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      add(_finishedButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                                  GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

      _finishedButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            finishedButton_actionPerformed(e);
         }
      });
   }

   static void gotoPanel(StatRecord[] stats)
   {
      // Build Screen
      EgoClient.frame.setVisible(false);
      Shared.setWaitCursor(EgoClient.frame, true);
      EgoClient.frame.setContentPane(new SummaryPanel(stats));
      EgoClient.frame.createMenuBar(EgoClient.VIEW_SUMMARY);
      EgoClient.frame.pack();
      EgoClient.frame.setSize(640, 530);
      Shared.setWaitCursor(EgoClient.frame, false);
      EgoClient.frame.setVisible(true);
   }

   static void gotoPanel()
   {
      final ProgressMonitor progressMonitor = new ProgressMonitor(EgoClient.frame, "Calculating Statistics", "", 0, 100);
      final SwingWorker worker = new SwingWorker() {
         public Object construct()
         {
            // Build Screen
            EgoClient.frame.setVisible(false);
            Shared.setWaitCursor(EgoClient.frame, true);
            EgoClient.frame.setContentPane(new SummaryPanel(progressMonitor));
            EgoClient.frame.createMenuBar(EgoClient.VIEW_SUMMARY);
            EgoClient.frame.pack();
            EgoClient.frame.setSize(640, 530);

            return EgoClient.frame;
         }

         public void finished()
         {
            Shared.setWaitCursor(EgoClient.frame, false);
            EgoClient.frame.setVisible(true);

            if (progressMonitor.isCanceled())
            {
               SourceSelectPanel.gotoPanel(false);
            }
            progressMonitor.close();
         }
      };

      progressMonitor.setProgress(0);
      progressMonitor.setMillisToDecideToPopup(0);
      progressMonitor.setMillisToPopup(0);

      worker.start();
   }

   private void finishedButton_actionPerformed(ActionEvent e)
   {
      SourceSelectPanel.gotoPanel(false);
   }

   private void loadInterviewArray(ProgressMonitor progress)
   {
      File intPath = new File(EgoClient.storage.getPackageFile().getParent(), "/Interviews/");
      File istPath = new File(EgoClient.storage.getPackageFile().getParent(), "/Statistics/");
      String[] intFiles = DirList.getDirList(intPath, "int");
      Set istFileSet = new HashSet();
      int i = 0, p = 0;

      istPath.mkdir();

      progress.setMinimum(0);
      progress.setMaximum(intFiles.length * 2);

      for (i = 0; (i < intFiles.length) && !progress.isCanceled(); i++)
      {
         progress.setProgress(++p);

         String thisIntFileName  = intFiles[i];
         File intFile            = new File(intPath, thisIntFileName);
         File thisIstFile, thisMatrixFile, thisWeightedMatrixFile, thisAlterSummaryFile;

         // Check that this has the correct Study Id
         try
         {
            String istPathString    = istPath.getCanonicalPath();
            thisIstFile             = EgoStore.int2ist(istPathString, intFiles[i]);
            thisMatrixFile          = EgoStore.int2matrix(istPathString, intFiles[i]);
            thisWeightedMatrixFile  = EgoStore.int2weightedmatrix(istPathString, intFiles[i]);
            thisAlterSummaryFile    = EgoStore.int2alterSummary(istPathString, intFiles[i]);

            if (!(thisIstFile.exists() && thisMatrixFile.exists() && thisWeightedMatrixFile.exists() && thisAlterSummaryFile.exists()))
            {
               Document document = new Document(intFile);
               Element root = document.getRoot();
               long id = Long.parseLong(root.getAttribute("StudyId"));

               if (id == EgoClient.study.getStudyId())
               {
                  EgoClient.storage.generateStatisticsFile(intFile);
                  istFileSet.add(thisIstFile);
               }
            }
            else
            {
               // IST files exists, check for compliance
               Document document = new Document(thisIstFile);
               Element root = document.getRoot();
               long id = Long.parseLong(root.getAttribute("StudyId"));
               String creator = root.getAttribute("Creator");

               if (id == EgoClient.study.getStudyId())
               {
                  //Commented this out because it would not create a new IST file when new measures are added
                  if (creator.equals(Shared.version))
                  {
                     istFileSet.add(thisIstFile);
                  }
                  else
                  {
                     EgoClient.storage.generateStatisticsFile(intFile);
                     istFileSet.add(thisIstFile);
                  }
               }
            }
         }
         catch (Exception ignored)
         {
         }
      }

      _stats = new StatRecord[istFileSet.size()];

      progress.setMaximum(intFiles.length + istFileSet.size());
      for (Iterator it = istFileSet.iterator(); it.hasNext() && !progress.isCanceled();)
      {
         progress.setProgress(++p);

         try
         {
            Document document = new Document((File) it.next());
            Element root = document.getRoot();

            StatRecord rec = new StatRecord(root);

            if (rec != null)
            {
               _stats[_recordCount++] = rec;
            }
         }
         catch (Exception ignored)
         {
            ignored.printStackTrace();
         }
      }
   }

   public void writeStudySummary(PrintWriter w)
   {
      Iterator it;
      StatRecord stat = _stats[0];
      DecimalFormat percentFormatter = new DecimalFormat("#.##");

      if (_stats.length == 0) { return; }

      /*******
       * Column Headers
       */
      w.print("Respondant_Name");
      it = stat.egoAnswers.iterator();
      while (it.hasNext())
      {
         w.print(", " + FileHelpers.formatForCSV(((StatRecord.EgoAnswer) it.next()).title));
      }

      it = stat.alterAnswers.iterator();
      while (it.hasNext())
      {
         StatRecord.AlterAnswer answer = (StatRecord.AlterAnswer) it.next();
         String title = FileHelpers.formatForCSV(answer.title);

         if (answer.selections.length == 1)
         {
            w.print(", " + title + "_mn");
         }
         else
         {
            for (int i = 0; i < answer.selections.length; i++)
            {
               w.print(", " + title + i + "N");
               w.print(", " + title + i + "P");
            }
         }
      }
      /*w.println(
       ", Max_Deg_Name, Max_Deg_Value, Max_Close_Name, Max_Close_Value"
       + ", Max_Between_Name, Max_Between_Value, #_Cliques, #_Components");*/
      w.println(", Max_Deg_Name, Max_Deg_Value, Max_Close_Name, Max_Close_Value"
                + ", Max_Between_Name, Max_Between_Value, N_Cliques, N_Components, Degree_Mean"
                + ", Closeness_Mean, Between_Mean, DegreeNC, ClosenessNC, BetweenNC" + ", N_Isolates, N_Dyads");

      /*******
       * Data Lines
       */
      for (int i = 0; i < _recordCount; i++)
      {
         stat = _stats[i];

         w.print(FileHelpers.formatForCSV(stat.name));

         it = stat.egoAnswers.iterator();
         while (it.hasNext())
         {
            w.print(", " + ((StatRecord.EgoAnswer) it.next()).index);
         }

         it = stat.alterAnswers.iterator();
         while (it.hasNext())
         {
            StatRecord.AlterAnswer answer = (StatRecord.AlterAnswer) it.next();

            if (answer.selections.length == 1)
            {
               if ((answer.count == 0) || (answer.totals[0] == 0))
               {
                  w.print(", " + 0);
               }
               else
               {
                  w.print(", " + ((float) answer.totals[0] / answer.count));
               }
            }
            else
            {
               for (int j = 0; j < answer.selections.length; j++)
               {
                  w.print(", " + answer.totals[j]);

                  if ((answer.count == 0) || (answer.totals[j] == 0))
                  {
                     w.print(", " + 0);
                  }
                  else
                  {
                     w.print(", " + percentFormatter.format((double) answer.totals[j] / answer.count));
                  }
               }
            }
         }

         w.println(", " + FileHelpers.formatForCSV(stat.degreeName) + ", " + stat.degreeValue + ", "
                   + FileHelpers.formatForCSV(stat.closenessName) + ", " + stat.closenessValue + ", "
                   + FileHelpers.formatForCSV(stat.betweenName) + ", " + stat.betweenValue + ", " + stat.numCliques
                   + ", " + stat.numComponents + ", " + stat.degreeMean + ", "
                   + (stat.closenessMean.floatValue() == -1 ? "." : stat.closenessMean.toString()) + ", "
                   + stat.betweenMean + ", " + stat.degreeNC + ", " + stat.closenessNC + ", " + stat.betweenNC + ","
                   + stat.numIsolates + "," + stat.numDyads);
      }
   }

   class SummaryModel
      extends StatTableModel
   {
      private final SummaryPanel summaryPanel;

      SummaryModel(SummaryPanel parent)
      {
         summaryPanel = parent;
      }

      public int getColumnCount()
      {
         if (summaryPanel._recordCount > 0)
         {
            return 17;
         }
         else
         {
            return 1;
         }
      }

      public Object getValueAt(int rowIndex, int columnIndex)
      {
         if (rowIndex < summaryPanel._recordCount)
         {
            try
            {
               switch (columnIndex)
               {
                  case 0:
                     return (summaryPanel._stats[rowIndex].name); /* Name */
                  case 1:
                     return (summaryPanel._stats[rowIndex].degreeName); /* Max Degree Name */
                  case 2:
                     return (summaryPanel._stats[rowIndex].degreeValue);
                  case 3:
                     return (summaryPanel._stats[rowIndex].closenessName); /* Max Closeness Name */
                  case 4:
                     return (summaryPanel._stats[rowIndex].closenessValue);
                  case 5:
                     return (summaryPanel._stats[rowIndex].betweenName); /* Max Betweenness Name */
                  case 6:
                     return (summaryPanel._stats[rowIndex].betweenValue);
                  case 7:
                     return (summaryPanel._stats[rowIndex].numCliques); /* # Cliques */
                  case 8:
                     return (summaryPanel._stats[rowIndex].numComponents); /* # Components */
                  case 9:
                     return (summaryPanel._stats[rowIndex].degreeMean);
                  case 10:
                     return (summaryPanel._stats[rowIndex].closenessMean);
                  case 11:
                     return (summaryPanel._stats[rowIndex].betweenMean);
                  case 12:
                     return (summaryPanel._stats[rowIndex].degreeNC);
                  case 13:
                     return (summaryPanel._stats[rowIndex].closenessNC);
                  case 14:
                     return (summaryPanel._stats[rowIndex].betweenNC);
                  case 15:
                     return (summaryPanel._stats[rowIndex].numIsolates); /* Components size 1 */
                  case 16:
                     return (summaryPanel._stats[rowIndex].numDyads); /* Components size 2*/
                  default:
                     return (null);
               }
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
               return (null);
            }
         }
         else
         {
            return (null);
         }
      }

      public int getRowCount()
      {
         if (summaryPanel._recordCount > 0)
         {
            return summaryPanel._recordCount;
         }
         else
         {
            return 0;
         }
      }

      public String getColumnName(int column)
      {
         if (summaryPanel._recordCount > 0)
         {
            switch (column)
            {
               case 0:
                  return ("Name");
               case 1:
                  return ("Degree Max");
               case 3:
                  return ("Closeness Max");
               case 5:
                  return ("Betweenness Max");
               case 7:
                  return ("# Cliques");
               case 8:
                  return ("# Components");
               case 9:
                  return ("Degree Mean");
               case 10:
                  return ("Closeness Mean");
               case 11:
                  return ("Betweenness Mean");
               case 12:
                  return ("Degree NC");
               case 13:
                  return ("Closeness NC");
               case 14:
                  return ("Betweenness NC");
               case 15:
                  return ("# Isolates");
               case 16:
                  return ("# Dyads");
               default:
                  return (null);
            }
         }
         else
         {
            return ("No matching interviews found");
         }
      }

      public int getResizeMode()
      {
         return JTable.AUTO_RESIZE_OFF;
      }
   }
}

/**
 * $Log$
 * Revision 1.1  2007/09/03 13:51:17  schoaff
 * Initial Checkin
 *
 * Revision 1.11  2004/04/08 15:06:07  admin
 * EgoClient now creates study summaries from Server
 * EgoAuthor now sets active study on server
 *
 */