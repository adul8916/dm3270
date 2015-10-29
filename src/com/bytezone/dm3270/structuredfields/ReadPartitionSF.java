package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.display.Screen;

public class ReadPartitionSF extends StructuredField
{
  private final byte partitionID;
  private String typeName;

  public ReadPartitionSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert data[0] == StructuredField.READ_PARTITION;
    partitionID = data[1];

    switch (data[2])
    {
      case (byte) 0x02:
        typeName = "Read Partition (Query)";
        break;

      case (byte) 0x03:
        typeName = "Read Partition (QueryList)";
        break;

      case Command.READ_BUFFER_F2:        // NB 0x02 would conflict with RPQ above
        typeName = "Read Partition (ReadBuffer)";
        break;

      case Command.READ_MODIFIED_F6:
        typeName = "Read Partition (ReadModified)";
        break;

      case Command.READ_MODIFIED_ALL_6E:
        typeName = "Read Partition (ReadModifiedAll)";
        break;

      default:
        typeName = String.format ("Unknown READ PARTITION type: %02X", data[2]);
    }
  }

  @Override
  public void process (Screen screen)
  {
    switch (data[2])
    {
      case (byte) 0x02:
        if (partitionID == (byte) 0xFF)                   // query operation
          reply = new ReadStructuredFieldCommand ();      // build a QueryReply
        else
          System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
        break;

      case (byte) 0x03:
        if (partitionID == (byte) 0xFF)                       // query operation
          switch (data[3])
          {
            case 0:
              System.out.println ("QCode List not written yet");
              break;

            case 1:
              System.out.println ("Equivalent + QCode List not written yet");
              break;

            case 2:
              reply = new ReadStructuredFieldCommand ();      // build a QueryReply
              break;

            default:
              System.out.printf ("Unknown %s: %02X%n", type, data[3]);
          }
        else
          System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
        break;

      // these three replies should be wrapped in an AID 0x88 (Read Structured Field)
      case Command.READ_BUFFER_F2:            // NB 0x02 would conflict with RPQ above
        reply = screen.readBuffer ();         // AID command
        break;

      case Command.READ_MODIFIED_F6:
      case Command.READ_MODIFIED_ALL_6E:
        reply = screen.readModifiedFields (data[2]);      // AID command
        break;

      default:
        System.out.printf ("Unknown ReadStructuredField type: %02X%n", data[2]);
    }
  }

  @Override
  public String brief ()
  {
    return String.format ("ReadPT: %s", reply);
  }

  @Override
  public String toString ()
  {
    StringBuilder text =
        new StringBuilder (String.format ("Struct Field : 01 Read Partition\n"));
    text.append (String.format ("   partition : %02X%n", partitionID));
    text.append (String.format ("   type      : %02X %s", data[2], typeName));
    return text.toString ();
  }
}