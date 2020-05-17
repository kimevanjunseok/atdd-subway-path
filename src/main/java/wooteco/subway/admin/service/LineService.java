package wooteco.subway.admin.service;

import org.springframework.stereotype.Service;
import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.LineStation;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineDetailResponse;
import wooteco.subway.admin.dto.LineRequest;
import wooteco.subway.admin.dto.LineStationCreateRequest;
import wooteco.subway.admin.dto.WholeSubwayResponse;
import wooteco.subway.admin.exception.NotExistPathException;
import wooteco.subway.admin.exception.NotFoundException;
import wooteco.subway.admin.repository.LineRepository;
import wooteco.subway.admin.repository.StationRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public Line save(Line line) {
        return lineRepository.save(line);
    }

    public List<Line> showLines() {
        return lineRepository.findAll();
    }

    public void updateLine(Long id, LineRequest request) {
        Line persistLine = lineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("%d를 가진 Line을 찾을 수 없습니다.", id)));
        persistLine.update(request.toLine());
        lineRepository.save(persistLine);
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long id, LineStationCreateRequest request) {
        Line line = lineRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("%d를 가진 Line을 찾을 수 없습니다.", id)));
        LineStation lineStation = new LineStation(request.getPreStationId(), request.getStationId(), request.getDistance(), request.getDuration());
        line.addLineStation(lineStation);

        lineRepository.save(line);
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new NotFoundException(String.format("%d를 가진 Line을 찾을 수 없습니다.", lineId)));
        line.removeLineStationById(stationId);
        lineRepository.save(line);
    }

    public LineDetailResponse findLineWithStationsById(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("%d를 가진 Line을 찾을 수 없습니다.", id)));
        List<Station> stations = stationRepository.findAllById(line.getLineStationsId());
        return LineDetailResponse.of(line, stations);
    }

    public WholeSubwayResponse wholeLines() {
        List<Line> lines = showLines();
        List<Long> wholeStationIds = getWholeStationIds(lines);
        List<Station> wholeStations = stationRepository.findAllById(wholeStationIds);

        List<LineDetailResponse> lineDetailResponses = getLineDetailResponses(lines, wholeStations);
        return WholeSubwayResponse.of(lineDetailResponses);
    }

    private List<Long> getWholeStationIds(List<Line> lines) {
        return lines.stream()
                .map(Line::getLineStationsId)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<LineDetailResponse> getLineDetailResponses(List<Line> lines, List<Station> wholeStations) {
        return lines.stream()
                .map(line -> LineDetailResponse.of(line, line.getMatchingStations(wholeStations)))
                .collect(Collectors.toList());
    }

    public Station findStationWithName(String name) {
        return stationRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(String.format("%s 이름을 가진 역이 존재하지 않습니다.", name)));
    }

    public List<Station> findAllStations() {
        return stationRepository.findAll();
    }
}
