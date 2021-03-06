package br.gov.servicos.frontend;

import br.gov.servicos.config.DestaquesConfig;
import br.gov.servicos.piwik.PiwikClient;
import br.gov.servicos.piwik.PiwikPage;
import br.gov.servicos.servico.Servico;
import br.gov.servicos.servico.ServicoRepository;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
@FieldDefaults(level = PRIVATE, makeFinal = true)
class IndexController {

    PiwikClient piwikClient;
    ServicoRepository servicos;
    DestaquesConfig destaques;

    @Autowired
    IndexController(ServicoRepository servicos, DestaquesConfig destaques, PiwikClient piwikClient) {
        this.servicos = servicos;
        this.destaques = destaques;
        this.piwikClient = piwikClient;
    }

    @RequestMapping("/")
    ModelAndView index() {
        return new ModelAndView("index", "destaques", servicosParaExibir());
    }

    @RequestMapping("/maisAcessados")
    ModelAndView maisAcessados() {
        return new ModelAndView("index", "destaques", servicosParaExibirMaisAcessados());
    }

    private List<Servico> servicosParaExibirMaisAcessados() {
        return concat(servicosMaisAcessados(), outrosServicos())
                .limit(15)
                .collect(toList());
    }

    private Stream<Servico> servicosMaisAcessados() {
        log.debug("Piwik: listando serviços mais acessados...");
        Stream<Servico> servicos = this.piwikClient.getPageUrls("week", "yesterday").stream()
                .filter(PiwikPage::isServicoUrl)
                .sorted((a, b) -> a.getUniqueVisitors().compareTo(b.getUniqueVisitors()))
                .map(p -> p.getUrl().replace("/servico/", ""))
                .map(this.servicos::findOne)
                .filter(Objects::nonNull);

        log.debug("Piwik: serviços mais acessados obtidos");
        return servicos;
    }

    private List<Servico> servicosParaExibir() {
        return concat(buscaDestaquesSeNecessario(), outrosServicos())
                .limit(15)
                .collect(toList());
    }

    private Stream<Servico> buscaDestaquesSeNecessario() {
        if (destaques.getServicos().isEmpty())
            return empty();

        return destaques.getServicos().stream()
                .map(servicos::findOne)
                .filter(Objects::nonNull);
    }

    private Stream<Servico> outrosServicos() {
        return stream(servicos.findAll(sorted()).spliterator(), false)
                .filter(s -> !destaques.getServicos().contains(s.getId()));
    }

    private PageRequest sorted() {
        return new PageRequest(0, 15, new Sort(DESC, "titulo"));
    }

}
