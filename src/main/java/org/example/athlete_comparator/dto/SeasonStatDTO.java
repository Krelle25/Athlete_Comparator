package org.example.athlete_comparator.dto;

public class SeasonStatDTO {
    private int season;
    private int type; // 2 = Regular Season | 3 = Postseason
    private int gp; // GP = Games Played
    private double min; // minutesPerGame
    private double pts; // pointsPerGame
    private double ast; // assistsPerGame
    private double reb; // reboundsPerGame
    private double stl; // stealsPerGame
    private double blk; // blocksPerGame
    private double tov; // turnoversPerGame
    private double fgm, fga, tpm, tpa, ftm, fta; // per game
    private Double ts; // derived
    private Double efg; // derived
    private Double per75Pts, per75Ast, per75Reb; // eksempel

    public SeasonStatDTO() {
    }

    public SeasonStatDTO(int season, int type, int gp, double min, double pts, double ast, double reb, double stl, double blk, double tov, double fgm, double fga, double tpm, double tpa, double ftm, double fta, Double ts, Double efg, Double per75Pts, Double per75Ast, Double per75Reb) {
        this.season = season;
        this.type = type;
        this.gp = gp;
        this.min = min;
        this.pts = pts;
        this.ast = ast;
        this.reb = reb;
        this.stl = stl;
        this.blk = blk;
        this.tov = tov;
        this.fgm = fgm;
        this.fga = fga;
        this.tpm = tpm;
        this.tpa = tpa;
        this.ftm = ftm;
        this.fta = fta;
        this.ts = ts;
        this.efg = efg;
        this.per75Pts = per75Pts;
        this.per75Ast = per75Ast;
        this.per75Reb = per75Reb;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getGp() {
        return gp;
    }

    public void setGp(int gp) {
        this.gp = gp;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getPts() {
        return pts;
    }

    public void setPts(double pts) {
        this.pts = pts;
    }

    public double getAst() {
        return ast;
    }

    public void setAst(double ast) {
        this.ast = ast;
    }

    public double getReb() {
        return reb;
    }

    public void setReb(double reb) {
        this.reb = reb;
    }

    public double getStl() {
        return stl;
    }

    public void setStl(double stl) {
        this.stl = stl;
    }

    public double getBlk() {
        return blk;
    }

    public void setBlk(double blk) {
        this.blk = blk;
    }

    public double getTov() {
        return tov;
    }

    public void setTov(double tov) {
        this.tov = tov;
    }

    public double getFgm() {
        return fgm;
    }

    public void setFgm(double fgm) {
        this.fgm = fgm;
    }

    public double getFga() {
        return fga;
    }

    public void setFga(double fga) {
        this.fga = fga;
    }

    public double getTpm() {
        return tpm;
    }

    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    public double getTpa() {
        return tpa;
    }

    public void setTpa(double tpa) {
        this.tpa = tpa;
    }

    public double getFtm() {
        return ftm;
    }

    public void setFtm(double ftm) {
        this.ftm = ftm;
    }

    public double getFta() {
        return fta;
    }

    public void setFta(double fta) {
        this.fta = fta;
    }

    public Double getTs() {
        return ts;
    }

    public void setTs(Double ts) {
        this.ts = ts;
    }

    public Double getEfg() {
        return efg;
    }

    public void setEfg(Double efg) {
        this.efg = efg;
    }

    public Double getPer75Pts() {
        return per75Pts;
    }

    public void setPer75Pts(Double per75Pts) {
        this.per75Pts = per75Pts;
    }

    public Double getPer75Ast() {
        return per75Ast;
    }

    public void setPer75Ast(Double per75Ast) {
        this.per75Ast = per75Ast;
    }

    public Double getPer75Reb() {
        return per75Reb;
    }

    public void setPer75Reb(Double per75Reb) {
        this.per75Reb = per75Reb;
    }
}