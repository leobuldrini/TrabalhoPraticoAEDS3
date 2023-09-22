package models;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Breach {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.US);

    public int id;
    public String company;
    public long recordsLost;
    public LocalDate date;
    public String detailedStory;
    public String[] sectorAndMethod;

    public Breach(int id, String company, long recordsLost, LocalDate date, String detailedStory, String[] sectorAndMethod){
        this.id = id;
        this.company = company;
        this.recordsLost = recordsLost;
        this.date = date;
        this.detailedStory = detailedStory;
        this.sectorAndMethod = sectorAndMethod;
    };
    public Breach(){
        this.id = -1;
        this.recordsLost = -1;
    };

    public void update(Breach breach) {
        if (breach.company != null && !breach.company.isEmpty()) {
            this.company = breach.company;
        }
        if(breach.date != null && breach.date.isAfter(LocalDate.ofYearDay(1800, 1))){
            this.date = breach.date;
        }
        if(breach.detailedStory != null && !breach.detailedStory.isEmpty()){
            this.detailedStory = breach.detailedStory;
        }
        if(breach.recordsLost >= 0){
            this.recordsLost = breach.recordsLost;
        }
        if(breach.sectorAndMethod != null && breach.sectorAndMethod.length > 0){
            this.sectorAndMethod = breach.sectorAndMethod;
        }
    }

    public String toString(){
        DecimalFormat df= new DecimalFormat("#,##0.00");//formata o valor dos pontos
        String fullList = "";
        for(int i = 0; i < sectorAndMethod.length; i++){
            fullList += sectorAndMethod[i] + ",";
        }
        fullList = fullList.substring(0, fullList.length() - 1);
        return "\nID:"+id +
                "\nCompany:"+this.company +
                "\nRecords Lost:"+ df.format(recordsLost) +
                "\nDate:"+ date +
                "\nDetailed Story:"+ detailedStory +
                "\nsectorAndMethod:"+ fullList;
    }

    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        String fullList = "";
        for(int i = 0; i < sectorAndMethod.length; i++){
            fullList += sectorAndMethod[i] + ",";
        }
        fullList = fullList.substring(0, fullList.length() - 1);

        dos.writeInt(id);
        dos.writeUTF(company);
        dos.writeLong(recordsLost);
        dos.writeUTF(date.toString());
        dos.writeUTF(detailedStory);
        dos.writeUTF(fullList);

        return baos.toByteArray();
    }

    public void fromByteArray(byte ba[]) throws IOException{

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id=dis.readInt();
        company=dis.readUTF();
        recordsLost=dis.readLong();
        date=LocalDate.parse(dis.readUTF(), formatter);
        detailedStory=dis.readUTF();

        String fullList = dis.readUTF();
        sectorAndMethod = fullList.split(",");
    }
}
