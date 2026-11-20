package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(unique = true, name = "steam_app_id", nullable = false)
    private String steamAppId;
    @Column(nullable = false)
    private String name;
    /**
     * URL da capa devolvida pela Steam (header_image). Fica nula nos jogos cadastrados antes
     * desta coluna existir — nesse caso o DTO monta a URL antiga como fallback.
     * A Steam mudou o CDN: montar "cdn.cloudflare.../header.jpg" na mão quebra em vários jogos.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public Game(String steamAppId, String name) {
        this(steamAppId, name, null);
    }

    public Game(String steamAppId, String name, String imageUrl) {
        this.steamAppId = steamAppId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    protected Game() {}

    public String getName(){
        return name;
    }
    public int getId() {
        return id;
    }
    public String getSteamAppId() {
        return steamAppId;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void atualizarNome(String newName){
        this.name = newName;
    }
    public void atualizarImagem(String novaImagem) {
        this.imageUrl = novaImagem;
    }
}
