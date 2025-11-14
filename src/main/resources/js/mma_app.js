// API Base URL
const API_BASE = '/api/mma';

// State management
let selectedFighterA = null;
let selectedFighterB = null;

// DOM Elements
const searchInputA = document.getElementById('searchA');
const searchInputB = document.getElementById('searchB');
const resultsA = document.getElementById('resultsA');
const resultsB = document.getElementById('resultsB');
const selectedA = document.getElementById('selectedA');
const selectedB = document.getElementById('selectedB');
const compareBtn = document.getElementById('compareBtn');
const loading = document.getElementById('loading');
const comparisonResults = document.getElementById('comparisonResults');
const fighterStats = document.getElementById('fighterStats');
const fighterInfo = document.getElementById('fighterInfo');
const statsA = document.getElementById('statsA');
const statsB = document.getElementById('statsB');
const infoA = document.getElementById('infoA');
const infoB = document.getElementById('infoB');

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
function displaySearchResults(fighters, resultsDiv) {
    if (fighters.length === 0) {
        resultsDiv.innerHTML = '<div class="no-results">No fighters found</div>';
        resultsDiv.classList.add('active');
        return;
    }

    resultsDiv.innerHTML = fighters.map(fighter => {
        const fighterID = fighter.ID || fighter.id || fighter.athleteId;
        const headshotUrl = fighter.headshotUrl || '';
        const nickname = fighter.nickname || '';
        const weightClass = fighter.weightClass || '';

        return `
            <div class="result-item" 
                data-fighter-id="${fighterID}" 
                data-fighter-name="${fighter.name}" 
                data-headshot-url="${headshotUrl}" 
                data-nickname="${nickname}" 
                data-weightclass="${weightClass}">
                ${headshotUrl ? `<img src="${headshotUrl}" alt="${fighter.name}" class="fighter-headshot">` : ''}
                <div class="fighter-info">
                    <strong>${fighter.name}</strong>
                    ${nickname ? `<span class="nickname">"${nickname}"</span>` : ''}
                    ${weightClass ? `<span class="weightClass">${weightClass}</span>` : ''}
                </div>
            </div>
        `;
    }).join('');

    resultsDiv.classList.add('active');
}

// Search fighters
async function searchFighters(query, resultsDiv) {
    if (!query || query.length < 2) {
        resultsDiv.innerHTML = '';
        resultsDiv.classList.remove('active');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error('Search failed');
        
        const fighters = await response.json();
        displaySearchResults(fighters, resultsDiv);
    } catch (error) {
        console.error('Search error:', error);
        resultsDiv.innerHTML = '<div class="error">Search failed. Please try again.</div>';
    }
}

// Fetch and display fighter info and stats
async function fetchAndDisplayFighterData() {
    try {
        // Fetch info, stats, and records for both fighters in parallel
        const [infoA_data, infoB_data, statsA_data, statsB_data, recordA_data, recordB_data] = await Promise.all([
            fetch(`${API_BASE}/fighters/${selectedFighterA.id}/info`).then(r => r.json()),
            fetch(`${API_BASE}/fighters/${selectedFighterB.id}/info`).then(r => r.json()),
            fetch(`${API_BASE}/fighters/${selectedFighterA.id}/stats`).then(r => r.json()),
            fetch(`${API_BASE}/fighters/${selectedFighterB.id}/stats`).then(r => r.json()),
            fetch(`${API_BASE}/fighters/${selectedFighterA.id}/record`).then(r => r.json()),
            fetch(`${API_BASE}/fighters/${selectedFighterB.id}/record`).then(r => r.json())
        ]);

        displayFighterInfo(infoA_data, infoA);
        displayFighterInfo(infoB_data, infoB);
        
        displayFighterStats(statsA_data, recordA_data, selectedFighterA, statsA);
        displayFighterStats(statsB_data, recordB_data, selectedFighterB, statsB);

        fighterInfo.classList.remove('hidden');
        fighterStats.classList.remove('hidden');
    } catch (error) {
        console.error('Error fetching fighter data:', error);
    }
}

// Display fighter info
function displayFighterInfo(info, container) {
    if (!info) {
        container.innerHTML = '<div class="no-stats">No information available</div>';
        return;
    }

    container.innerHTML = `
        <h3>${info.name || 'Unknown'}</h3>
        ${info.nickname ? `<p class="info-nickname">"${info.nickname}"</p>` : ''}

        <div class="info-section">
        <div class="info-row">
                <span>Gender:</span>
                <strong>${info.gender || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Weight Class:</span>
                <strong>${info.weightClass || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Height:</span>
                <strong>${info.height || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Weight:</span>
                <strong>${info.weight || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Reach:</span>
                <strong>${info.reach || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Age:</span>
                <strong>${info.age || 'N/A'}</strong>
            </div>
            <div class="info-row">
                <span>Country:</span>
                <strong>${info.country || 'N/A'}</strong>
            </div>
        </div>
    `;
}

// Display fighter stats
function displayFighterStats(stats, record, fighter, container) {
    if (!stats) {
        container.innerHTML = '<div class="no-stats">No statistics available</div>';
        return;
    }

    container.innerHTML = `
        <h3>${fighter.name}</h3>
        <p class="stat-position">${fighter.weightClass || 'Fighter'}</p>
        
        <div class="career-summary">
            <h4>Career Summary</h4>
            <div class="stat-row">
                <span>Record:</span>
                <strong>${record ? `${record.wins || 0}-${record.losses || 0}-${record.draws || 0}` : 'N/A'}</strong>
            </div>
            <div class="stat-row">
                <span>Win Rate:</span>
                <strong>${record && record.winRate ? record.winRate.toFixed(1) : '0'}%</strong>
            </div>
        </div>

        <div class="stat-averages">
            <h4>Fight Stats (per 15 min)</h4>
            <div class="stat-row">
                <span>Strikes Landed:</span>
                <strong>${stats.strikeLPM ? stats.strikeLPM.toFixed(2) : 'N/A'}</strong>
            </div>
            <div class="stat-row">
                <span>Strike Accuracy:</span>
                <strong>${stats.strikeAccuracy ? stats.strikeAccuracy.toFixed(1) : 'N/A'}%</strong>
            </div>
            <div class="stat-row">
                <span>Takedowns:</span>
                <strong>${stats.takedownAvg ? stats.takedownAvg.toFixed(2) : 'N/A'}</strong>
            </div>
            <div class="stat-row">
                <span>Takedown Accuracy:</span>
                <strong>${stats.takedownAccuracy ? stats.takedownAccuracy.toFixed(1) : 'N/A'}%</strong>
            </div>
            <div class="stat-row">
                <span>Submissions:</span>
                <strong>${stats.submissionAvg ? stats.submissionAvg.toFixed(2) : 'N/A'}</strong>
            </div>
        </div>

        <div class="peak-season">
            <h4>Finish Rates</h4>
            <div class="stat-row">
                <span>KO/TKO:</span>
                <strong>${stats.koPercentage ? stats.koPercentage.toFixed(1) : 'N/A'}% / ${stats.tkoPercentage ? stats.tkoPercentage.toFixed(1) : 'N/A'}%</strong>
            </div>
            <div class="stat-row">
                <span>Decision:</span>
                <strong>${stats.decisionPercentage ? stats.decisionPercentage.toFixed(1) : 'N/A'}%</strong>
            </div>
        </div>
    `;
}

// Update compare button state
function updateCompareButton() {
    compareBtn.disabled = !selectedFighterA || !selectedFighterB;

    // Show/update info and stats when both fighters are selected
    if (selectedFighterA && selectedFighterB) {
        fetchAndDisplayFighterData();
    } else {
        fighterInfo.classList.add('hidden');
        fighterStats.classList.add('hidden');
    }
}

// Clear fighter selection
function clearFighter(fighterKey) {
    if (fighterKey === 'A') {
        selectedFighterA = null;
        selectedA.innerHTML = '';
    } else {
        selectedFighterB = null;
        selectedB.innerHTML = '';
    }
    updateCompareButton();
}

// Select fighter
function selectFighter(id, name, fighterKey, headshotUrl, nickname, weightClass) {
    const fighter = { id, name, headshotUrl, nickname, weightClass };
    
    if (fighterKey === 'A') {
        selectedFighterA = fighter;
        selectedA.innerHTML = createSelectedFighterHTML(fighter, 'A');
    } else {
        selectedFighterB = fighter;
        selectedB.innerHTML = createSelectedFighterHTML(fighter, 'B');
    }

    updateCompareButton();
    
    // Hide search results
    if (fighterKey === 'A') {
        resultsA.classList.remove('active');
        searchInputA.value = '';
    } else {
        resultsB.classList.remove('active');
        searchInputB.value = '';
    }
}

// Create selected fighter HTML
function createSelectedFighterHTML(fighter, key) {
    return `
        <div class="selected-info">
            ${fighter.headshotUrl ? `<img src="${fighter.headshotUrl}" alt="${fighter.name}" class="selected-headshot">` : ''}
            <div class="fighter-details">
                <div class="fighter-name">${fighter.name}</div>
                ${fighter.nickname ? `<div class="fighter-nickname">"${fighter.nickname}"</div>` : ''}
                ${fighter.weightClass ? `<div class="fighter-weightClass">${fighter.weightClass}</div>` : ''}
            </div>
            <button class="remove-btn" onclick="clearFighter('${key}')">×</button>
        </div>
    `;
}

// Display comparison results
function displayComparisonResults(result) {
    // Update winner banner
    document.getElementById('overallWinner').textContent = result.overallWinner || 'Tie';

    // Update 1v1 prediction
    document.getElementById('oneVsOne').textContent = result.oneVsOnePrediction || 'N/A';

    // Update fighter strengths with names
    document.getElementById('athlete1NameStrengths').textContent = 
        `${selectedFighterA.name} Strengths`;
    document.getElementById('athlete1Strengths').textContent = 
        result.athlete1Strengths || 'N/A';

    document.getElementById('athlete2NameStrengths').textContent = 
        `${selectedFighterB.name} Strengths`;
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

// Compare fighters
async function compareFighters() {
    if (!selectedFighterA || !selectedFighterB) return;
    
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
                aID: selectedFighterA.id,
                bID: selectedFighterB.id
            })
        });

        if (!response.ok) throw new Error('Comparison failed');
        
        const result = await response.json();
        displayComparisonResults(result);
    } catch (error) {
        console.error('Comparison error:', error);
        alert('Comparison failed. Please try again.');
    } finally {
        loading.classList.add('hidden');
        compareBtn.disabled = false;
    }
}

// Event listeners
searchInputA.addEventListener('input', debounce((e) => {
    searchFighters(e.target.value, resultsA);
}, 300));

searchInputB.addEventListener('input', debounce((e) => {
    searchFighters(e.target.value, resultsB);
}, 300));

// Click handlers for search results
resultsA.addEventListener('click', (e) => {
    const item = e.target.closest('.result-item');
    if (item) {
        selectFighter(
            item.dataset.fighterId,
            item.dataset.fighterName,
            'A',
            item.dataset.headshotUrl,
            item.dataset.nickname,
            item.dataset.weightclass
        );
    }
});

resultsB.addEventListener('click', (e) => {
    const item = e.target.closest('.result-item');
    if (item) {
        selectFighter(
            item.dataset.fighterId,
            item.dataset.fighterName,
            'B',
            item.dataset.headshotUrl,
            item.dataset.nickname,
            item.dataset.weightclass
        );
    }
});

// Compare button
compareBtn.addEventListener('click', compareFighters);

// Close search results when clicking outside
document.addEventListener('click', (e) => {
    if (!searchInputA.contains(e.target) && !resultsA.contains(e.target)) {
        resultsA.classList.remove('active');
    }
    if (!searchInputB.contains(e.target) && !resultsB.contains(e.target)) {
        resultsB.classList.remove('active');
    }
});
