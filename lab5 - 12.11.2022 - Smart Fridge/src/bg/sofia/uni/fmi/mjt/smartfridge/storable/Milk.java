package bg.sofia.uni.fmi.mjt.smartfridge.storable;

import bg.sofia.uni.fmi.mjt.smartfridge.storable.type.StorableType;

import java.time.LocalDate;

public class Milk implements Storable {
    private String name;
    private StorableType type;
    private LocalDate expiration;

    public Milk(String name, StorableType type, LocalDate expiration) {
        this.name = name;
        this.type = type;
        this.expiration = expiration;
    }

    @Override
    public StorableType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalDate getExpiration() {
        return expiration;
    }

    @Override
    public boolean isExpired() {
        return expiration.isBefore(LocalDate.now());
    }
}
