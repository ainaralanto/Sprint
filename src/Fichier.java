package mg.p16.classe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class Fichier {
    private String name;
    private String path;
    private byte[] contenu;

    public Fichier(String name, String path) throws IOException {
        this.name = name;
        this.path = path;
        this.contenu = Files.readAllBytes(Paths.get(path));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }

    public void sauvegarder() throws IOException {
        Files.write(Paths.get(path), contenu);
    }
}
