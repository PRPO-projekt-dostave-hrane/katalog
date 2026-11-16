package si.unilj.fri.prpo.katalog.api.vir;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import si.unilj.fri.prpo.katalog.api.RestavracijeApi;
import si.unilj.fri.prpo.katalog.api.model.Restavracija;
import si.unilj.fri.prpo.katalog.api.model.SeznamRestavracij;

@ApplicationScoped
public class RestavracijeVir implements RestavracijeApi {
    private final Map<String, Restavracija> restavracije = new ConcurrentHashMap<>();

    public RestavracijeVir() {
        dodaj(new Restavracija().id("r-1").ime("Pizzeria Napoli")
                .kuhinja("italijanska").naslov("Italijanska ulica 23")
                .povprecnaOcena(new BigDecimal("4.6")));
        dodaj(new Restavracija().id("r-2").ime("Sushi Koi")
                .kuhinja("japonska").naslov("Japonska ulica 5"));
        dodaj(new Restavracija().id("r-3").ime("Gostilna Pri Lipi")
                .kuhinja("slovenska").naslov("Slovenska cesta 10")
                .povprecnaOcena(new BigDecimal("4.2")));
    }

    private void dodaj(Restavracija restavracija) {
        restavracije.put(restavracija.getId(), restavracija);
    }

    @Override
    public Response seznamRestavracij(String kuhinja, Integer limit, Integer offset) {
        List<Restavracija> seznam = restavracije.values().stream()
        .filter(r -> kuhinja == null || r.getKuhinja().equals(kuhinja))
        .sorted(Comparator.comparing(r -> r.getId()))
        .skip(offset)
        .limit(limit)
        .toList();
        return Response.ok(new SeznamRestavracij().items(seznam)).build();
    }

    @Override
    public Response podrobnostiRestavracije(String restavracijaId) {
        Restavracija restavracija = restavracije.get(restavracijaId);
        if (restavracija == null) {
            throw new NotFoundException("Restavracija ni bila najdena.");
        }
        return Response.ok(restavracija).build();
    }
}
