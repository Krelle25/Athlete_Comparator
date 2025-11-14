// API Base URL
const API_BASE = '/api/nba';

// State management
let selectedPlayerA = null;
let selectedPlayerB = null;

// DOM Elements
const searchInputA = document.getElementById('searchA');
const searchInputB = document.getElementById('searchB');
const playerAccolades = document.getElementById('playerAccolades');
const accoladesA = document.getElementById('accoladesA');
const accoladesB = document.getElementById('accoladesB');
const resultsA = document.getElementById('resultsA');
const resultsB = document.getElementById('resultsB');
const selectedA = document.getElementById('selectedA');
const selectedB = document.getElementById('selectedB');
const compareBtn = document.getElementById('compareBtn');
const loading = document.getElementById('loading');
const comparisonResults = document.getElementById('comparisonResults');
const playerStats = document.getElementById('playerStats');
const statsA = document.getElementById('statsA');
const statsB = document.getElementById('statsB');

// Debounce function for search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Display search results
function displaySearchResults(players, resultsDiv) {
    if (players.length === 0) {
        resultsDiv.innerHTML = '<div class="no-results">No players found</div>';
        resultsDiv.classList.add('active');
        return;
    }

    resultsDiv.innerHTML = players.map(player => {
        const playerId = player.ID || player.id || player.athleteId;
        const headshotUrl = player.headshotUrl || '';
        const team = player.team || '';
        const position = player.position || '';
        const height = (player.displayHeight || player.height || '').toString();
        const weight = (player.displayWeight || player.weight || '').toString();

        return `
            <div class="result-item" 
                data-player-id="${playerId}" 
                data-player-name="${player.name}" 
                data-headshot-url="${headshotUrl}" 
                data-team="${team}" 
                data-position="${position}"
                data-height="${height}"
                data-weight="${weight}">
                ${headshotUrl ? `<img src="${headshotUrl}" alt="${player.name}" class="player-headshot">` : ''}
                <div class="player-info">
                    <strong>${player.name}</strong>
                    ${position ? `<span class="position">${position}</span>` : ''}
                    ${team ? `<span class="team">${team}</span>` : ''}
                </div>
            </div>
        `;
    }).join('');

    resultsDiv.classList.add('active');
}

// Search players
async function searchPlayers(query, resultsDiv) {
    if (!query || query.length < 2) {
        resultsDiv.innerHTML = '';
        resultsDiv.classList.remove('active');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error('Search failed');
        
        const players = await response.json();
        displaySearchResults(players, resultsDiv);
    } catch (error) {
        console.error('Search error:', error);
        resultsDiv.innerHTML = '<div class="error">Search failed. Please try again.</div>';
    }
}

// Display individual player stats
function displayPlayerStats(statsData, player, container) {
    if (!statsData || statsData.length === 0) {
        container.innerHTML = `
            <div class="no-stats">
                <h3>${player.name}</h3>
                <p>No statistics available</p>
            </div>
        `;
        return;
    }

    // Separate regular season and playoff stats
    const regularSeasonStats = statsData.filter(s => s.type === 2);
    const playoffStats = statsData.filter(s => s.type === 3);

    // Determine which stats to show based on what's selected
    const statsToDisplay = regularSeasonStats.length > 0 ? regularSeasonStats : statsData;

    // Calculate career averages
    const totalGames = statsToDisplay.reduce((sum, s) => sum + s.gp, 0);
    const avgPts = (statsToDisplay.reduce((sum, s) => sum + s.pts, 0) / statsToDisplay.length).toFixed(1);
    const avgAst = (statsToDisplay.reduce((sum, s) => sum + s.ast, 0) / statsToDisplay.length).toFixed(1);
    const avgReb = (statsToDisplay.reduce((sum, s) => sum + s.reb, 0) / statsToDisplay.length).toFixed(1);
    const avgMin = (statsToDisplay.reduce((sum, s) => sum + s.min, 0) / statsToDisplay.length).toFixed(1);

    // Calculate shooting percentages
    const totalFgm = statsToDisplay.reduce((sum, s) => sum + s.fgm, 0);
    const totalFga = statsToDisplay.reduce((sum, s) => sum + s.fga, 0);
    const fgPct = totalFga > 0 ? ((totalFgm / totalFga) * 100).toFixed(1) : '0.0';

    const total3pm = statsToDisplay.reduce((sum, s) => sum + s.tpm, 0);
    const total3pa = statsToDisplay.reduce((sum, s) => sum + s.tpa, 0);
    const threePct = total3pa > 0 ? ((total3pm / total3pa) * 100).toFixed(1) : '0.0';

    // Find peak regular season (minimum 20 games to qualify)
    // Peak is determined by total production: PTS + REB + AST
    const qualifiedRegularStats = regularSeasonStats.filter(s => s.gp >= 20);
    const peakRegularSeason = qualifiedRegularStats.length > 0
        ? qualifiedRegularStats.reduce((max, s) => {
            const sTotal = s.pts + s.reb + s.ast;
            const maxTotal = max.pts + max.reb + max.ast;
            return sTotal > maxTotal ? s : max;
        }, qualifiedRegularStats[0])
        : null;

    // Build peak season HTML
    let peakSeasonHtml = '';
    if (peakRegularSeason) {
        const total = (peakRegularSeason.pts + peakRegularSeason.reb + peakRegularSeason.ast).toFixed(1);
        peakSeasonHtml = `
            <div class="peak-season">
                <h4>Peak Regular Season (${peakRegularSeason.season})</h4>
                <p>${peakRegularSeason.pts.toFixed(1)} PPG, ${peakRegularSeason.ast.toFixed(1)} APG, ${peakRegularSeason.reb.toFixed(1)} RPG</p>
                <p class="total-production">Total: ${total} (PPG+APG+RPG)</p>
                <p class="games-played">${peakRegularSeason.gp} games played</p>
            </div>
        `;
    }

    // Add playoff peak if available
    // Peak playoff run also determined by total production
    if (playoffStats.length > 0) {
        const peakPlayoff = playoffStats.reduce((max, s) => {
            const sTotal = s.pts + s.reb + s.ast;
            const maxTotal = max.pts + max.reb + max.ast;
            return sTotal > maxTotal ? s : max;
        }, playoffStats[0]);
        const total = (peakPlayoff.pts + peakPlayoff.reb + peakPlayoff.ast).toFixed(1);
        peakSeasonHtml += `
            <div class="peak-season playoff-peak">
                <h4>Peak Playoff Run (${peakPlayoff.season})</h4>
                <p>${peakPlayoff.pts.toFixed(1)} PPG, ${peakPlayoff.ast.toFixed(1)} APG, ${peakPlayoff.reb.toFixed(1)} RPG</p>
                <p class="total-production">Total: ${total} (PPG+APG+RPG)</p>
                <p class="games-played">${peakPlayoff.gp} playoff games</p>
            </div>
        `;
    }

    container.innerHTML = `
        <h3>${player.name}</h3>
        ${(
        [player.position || player.team || player.height || player.weight].some(Boolean)
    ) ? `<p class="stat-position">
              ${[player.position, player.team, player.height, player.weight].filter(Boolean).join(' • ')}
            </p>` : ''}
        
        <div class="career-summary">
            <h4>Career Overview</h4>
            <p><strong>Seasons:</strong> ${statsToDisplay.length} | <strong>Games:</strong> ${totalGames}</p>
        </div>
        
        <div class="stat-averages">
            <h4>Career Averages</h4>
            <div class="stat-row"><span>Points:</span> <strong>${avgPts}</strong></div>
            <div class="stat-row"><span>Assists:</span> <strong>${avgAst}</strong></div>
            <div class="stat-row"><span>Rebounds:</span> <strong>${avgReb}</strong></div>
            <div class="stat-row"><span>Minutes:</span> <strong>${avgMin}</strong></div>
            <div class="stat-row"><span>FG%:</span> <strong>${fgPct}%</strong></div>
            <div class="stat-row"><span>3P%:</span> <strong>${threePct}%</strong></div>
        </div>
        
        ${peakSeasonHtml}
    `;
}

// Fetch and display player stats
async function fetchAndDisplayStats() {
    const statsType = getSelectedStatsType();

    try {
        // Fetch stats and accolades for both players in parallel
        const [statsDataA, statsDataB, accoladesDataA, accoladesDataB] = await Promise.all([
            fetch(`${API_BASE}/athletes/${selectedPlayerA.id}/season-stats?type=${statsType}`).then(r => r.json()),
            fetch(`${API_BASE}/athletes/${selectedPlayerB.id}/season-stats?type=${statsType}`).then(r => r.json()),
            fetch(`${API_BASE}/athletes/${selectedPlayerA.id}/accolades`).then(r => r.json()),
            fetch(`${API_BASE}/athletes/${selectedPlayerB.id}/accolades`).then(r => r.json())
        ]);

        displayPlayerStats(statsDataA, selectedPlayerA, statsA);
        displayPlayerStats(statsDataB, selectedPlayerB, statsB);

        displayPlayerAccolades(accoladesDataA, selectedPlayerA, accoladesA);
        displayPlayerAccolades(accoladesDataB, selectedPlayerB, accoladesB);

        playerStats.classList.remove('hidden');
        playerAccolades.classList.remove('hidden');
    } catch (error) {
        console.error('Error fetching stats:', error);
    }
}

// Update compare button state
function updateCompareButton() {
    compareBtn.disabled = !selectedPlayerA || !selectedPlayerB;

    // Show/update stats when both players are selected
    if (selectedPlayerA && selectedPlayerB) {
        fetchAndDisplayStats();
    } else {
        playerStats.classList.add('hidden');
    }
}

// Clear player selection
function clearPlayer(playerKey) {
    if (playerKey === 'A') {
        selectedPlayerA = null;
        selectedA.innerHTML = '';
    } else {
        selectedPlayerB = null;
        selectedB.innerHTML = '';
    }
    updateCompareButton();
}

// Select player
function selectPlayer(
    id,
    name,
    playerKey,
    headshotUrl = '',
    team = '',
    position = '',
    height = '',
    weight = ''
) {
    // Convert id to number
    const playerId = parseInt(id, 10);
    
    const playerInfo = `
        <div class="player-details">
            <span class="player-name">${name}</span>
            ${position ? `<span class="player-position">${position}</span>` : ''}
            ${team ? `<span class="player-team">${team}</span>` : ''}
            ${(height || weight) ? `<span class="player-bio">${[height, weight].filter(Boolean).join(' • ')}</span>` : ''}
        </div>
    `;
    
    if (playerKey === 'A') {
        selectedPlayerA = { id: playerId, name, headshotUrl, team, position, height, weight };
        selectedA.innerHTML = `
            <div class="selected-info">
                ${headshotUrl ? `<img src="${headshotUrl}" alt="${name}" class="selected-headshot">` : ''}
                ${playerInfo}
                <button class="remove-btn" onclick="clearPlayer('A')">✕</button>
            </div>
        `;
        searchInputA.value = '';
        resultsA.innerHTML = '';
        resultsA.classList.remove('active');
    } else {
        selectedPlayerB = { id: playerId, name, headshotUrl, team, position, height, weight };
        selectedB.innerHTML = `
            <div class="selected-info">
                ${headshotUrl ? `<img src="${headshotUrl}" alt="${name}" class="selected-headshot">` : ''}
                ${playerInfo}
                <button class="remove-btn" onclick="clearPlayer('B')">✕</button>
            </div>
        `;
        searchInputB.value = '';
        resultsB.innerHTML = '';
        resultsB.classList.remove('active');
    }
    
    updateCompareButton();
}

// Display individual player accolades
function displayPlayerAccolades(accoladesData, player, container) {
    if (!accoladesData || !accoladesData.awards || accoladesData.awards.length === 0) {
        container.innerHTML = `
            <div class="no-accolades">
                <h3>${player.name}</h3>
                <p>No accolades available</p>
            </div>
        `;
        return;
    }

    const awardsHtml = accoladesData.awards.map(award => {
        const yearDisplay = award.year ? `<span class="award-year">(${award.year})</span>` : '';
        const descDisplay = award.description ? `<p class="award-desc">${award.description}</p>` : '';
        return `
            <div class="award-item">
                <h4 class="award-title">${award.title} ${yearDisplay}</h4>
                ${descDisplay}
            </div>
        `;
    }).join('');

    container.innerHTML = `
        <div class="accolades-content">
            <h3>${player.name}</h3>
            <div class="awards-list">
                ${awardsHtml}
            </div>
        </div>
    `;
}

// Get selected stats type
function getSelectedStatsType() {
    const selected = document.querySelector('input[name="statsType"]:checked');
    return parseInt(selected.value);
}

// Display comparison results
function displayComparisonResults(result) {
    // Update winner banner
    document.getElementById('overallWinner').textContent = result.overallWinner || 'Tie';

    // Update 1v1 prediction
    document.getElementById('oneVsOne').textContent = result.oneVsOnePrediction || 'N/A';

    // Update player strengths with names
    document.getElementById('athlete1NameStrengths').textContent =
        `${selectedPlayerA.name} Strengths`;
    document.getElementById('athlete1Strengths').textContent =
        result.athlete1Strengths || 'N/A';

    document.getElementById('athlete2NameStrengths').textContent =
        `${selectedPlayerB.name} Strengths`;
    document.getElementById('athlete2Strengths').textContent =
        result.athlete2Strengths || 'N/A';

    // Update conclusion
    document.getElementById('conclusion').textContent = result.conclusion || 'N/A';

    // Update full analysis
    document.getElementById('fullAnalysis').textContent =
        result.analysis || 'No detailed analysis available';

    // Show results
    comparisonResults.classList.remove('hidden');
    comparisonResults.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Compare players
async function comparePlayers() {
    if (!selectedPlayerA || !selectedPlayerB) return;

    const statsType = getSelectedStatsType();
    
    loading.classList.remove('hidden');
    comparisonResults.classList.add('hidden');
    compareBtn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/compare`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                aID: selectedPlayerA.id,
                bID: selectedPlayerB.id,
                type: statsType
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Response status:', response.status);
            console.error('Response error:', errorText);
            throw new Error(`Comparison failed: ${response.status} - ${errorText}`);
        }
        
        const result = await response.json();
        displayComparisonResults(result);
    } catch (error) {
        console.error('Comparison error:', error);
        alert('Comparison failed. Please try again.\n\nError: ' + error.message);
    } finally {
        loading.classList.add('hidden');
        compareBtn.disabled = false;
    }
}

// Event listeners
searchInputA.addEventListener('input', debounce((e) => {
    searchPlayers(e.target.value, resultsA);
}, 300));

searchInputB.addEventListener('input', debounce((e) => {
    searchPlayers(e.target.value, resultsB);
}, 300));

// Click handlers for search results
resultsA.addEventListener('click', (e) => {
    const item = e.target.closest('.result-item');
    if (item) {
        selectPlayer(
            item.dataset.playerId,
            item.dataset.playerName,
            'A',
            item.dataset.headshotUrl,
            item.dataset.team,
            item.dataset.position,
            item.dataset.height,
            item.dataset.weight
        );
    }
});

resultsB.addEventListener('click', (e) => {
    const item = e.target.closest('.result-item');
    if (item) {
        selectPlayer(
            item.dataset.playerId,
            item.dataset.playerName,
            'B',
            item.dataset.headshotUrl,
            item.dataset.team,
            item.dataset.position,
            item.dataset.height,
            item.dataset.weight
        );
    }
});

// Compare button
compareBtn.addEventListener('click', comparePlayers);

// Stats type change listener
document.querySelectorAll('input[name="statsType"]').forEach(radio => {
    radio.addEventListener('change', () => {
        if (selectedPlayerA && selectedPlayerB) {
            fetchAndDisplayStats();
        }
    });
});

// Close search results when clicking outside
document.addEventListener('click', (e) => {
    if (!e.target.closest('.player-box')) {
        resultsA.classList.remove('active');
        resultsB.classList.remove('active');
    }
});

// Make clearPlayer function global
window.clearPlayer = clearPlayer;