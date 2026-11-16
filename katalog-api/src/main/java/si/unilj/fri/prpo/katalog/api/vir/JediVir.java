package si.unilj.fri.prpo.katalog.api.vir;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.core.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import si.unilj.fri.prpo.katalog.api.JediApi;
import si.unilj.fri.prpo.katalog.api.model.Jed;
import si.unilj.fri.prpo.katalog.api.model.NovaJed;
import si.unilj.fri.prpo.katalog.api.model.SeznamJedi;
import si.unilj.fri.prpo.katalog.api.model.SpremembaJedi;
import si.unilj.fri.prpo.katalog.api.model.Cena;

@ApplicationScoped
public class JediVir implements JediApi {
    private final Map<String, Map<String, Jed>> jedi = new ConcurrentHashMap<>();

    public JediVir() {
        Map<String, Jed> napoli = new ConcurrentHashMap<>();
        dodaj(napoli, new Jed().id("j-1").ime("Margherita")
                .opis("Paradižnikova omaka, mocarela, bazilika")
                .cena(new Cena().znesek("9.50").valuta(Cena.ValutaEnum.EUR))
                .razpolozljiva(true));
        dodaj(napoli, new Jed().id("j-2").ime("Tiramisu")
                .cena(new Cena().znesek("5.00").valuta(Cena.ValutaEnum.EUR))
                .razpolozljiva(true));
        jedi.put("r-1", napoli);

        jedi.put("r-2", new ConcurrentHashMap<>());
        jedi.put("r-3", new ConcurrentHashMap<>());
    }

    private static void dodaj(Map<String, Jed> meni, Jed jed) {
        meni.put(jed.getId(), jed);
    }

    private Map<String, Jed> najdiRestavracijo(String restavracijaId) {
        Map<String, Jed> restavracija = jedi.get(restavracijaId);
        if (restavracija == null) {
            throw new NotFoundException("Restavracija ni bila najdena.");
        }
        return restavracija;
    }

    private Jed najdiJed(String restavracijaId, String jedId) {
        Map<String, Jed> restavracija = najdiRestavracijo(restavracijaId);
        Jed jed = restavracija.get(jedId);
        if (jed == null) {
            throw new NotFoundException("Jed ni bila najdena.");
        }
        return jed;
    }

    private static String etag(Jed jed) {
        return "\"" + Integer.toHexString(jed.hashCode()) + "\"";
    }
    
    @Override 
    public Response dodajJed(String restavracijaId, NovaJed novaJed) {
        Map<String, Jed> restavracija = najdiRestavracijo(restavracijaId);

        String id = "j-" + UUID.randomUUID();
        Jed jed = new Jed()
        .id(id)
        .ime(novaJed.getIme())
        .opis(novaJed.getOpis())
        .cena(novaJed.getCena())
        .razpolozljiva(novaJed.getRazpolozljiva());

        restavracija.put(jed.getId(), jed);
        URI lokacija = URI.create("/v1/restavracije/" + restavracijaId + "/jedi/" + id);
        return Response.created(lokacija).entity(jed).build();
    }

    @Override 
    public Response izbrisiJed(String restavracijaId, String jedId) {
        Map<String, Jed> restavracija = najdiRestavracijo(restavracijaId);
        if (restavracija.remove(jedId) == null) {
            throw new NotFoundException("Jed ni bila najdena.");
        }
        return Response.noContent().build();
    }

    @Override 
    public Response podrobnostiJedi(String restavracijaId, String jedId) {
        Jed jed = najdiJed(restavracijaId, jedId);
        synchronized (jed){ 
            return Response.ok(jed).header("ETag", etag(jed)).build();
        }
    }

    @Override 
    public Response seznamJedi(String restavracijaId) {
        Map<String, Jed> restavracija = najdiRestavracijo(restavracijaId);

        List<Jed> seznam = restavracija.values().stream()
        .sorted(Comparator.comparing(j -> j.getId()))
        .toList();

        return Response.ok(new SeznamJedi().items(seznam)).build();
    }

    @Override 
    public Response spremeniJed(String restavracijaId, String jedId, SpremembaJedi spremembaJedi, String ifMatch) {
        Jed jed = najdiJed(restavracijaId, jedId);
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new WebApplicationException("Manjka glava If-Match.", Response.Status.PRECONDITION_REQUIRED);
        }

        synchronized (jed) {
            if (!etag(jed).equals(ifMatch)) {
                throw new WebApplicationException("ETag se ne ujema s trenutno verzijo jedi.", Response.Status.PRECONDITION_FAILED);
            }

            if (spremembaJedi.getIme() != null) {
                jed.setIme(spremembaJedi.getIme());
            }
            if (spremembaJedi.getOpis() != null) {
                jed.setOpis(spremembaJedi.getOpis());
            }
            if (spremembaJedi.getCena() != null) {
                jed.setCena(spremembaJedi.getCena());
            }
            if (spremembaJedi.getRazpolozljiva() != null) {
                jed.setRazpolozljiva(spremembaJedi.getRazpolozljiva());
            }

            return Response.ok(jed).header("ETag", etag(jed)).build();
        }
    }
}