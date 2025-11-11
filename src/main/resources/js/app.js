// API Base URL
const API_BASE = '/api';

// State management
let selectedPlayerA = null;
let selectedPlayerB = null;

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

// Display search results
function displaySearchResults(players, resultsDiv) {
    if (players.length === 0) {
        resultsDiv.innerHTML = '<div class="no-results">No players found</div>';
        resultsDiv.classList.add('active');
        return;
    }

    console.log('Search results:', players); // Debug log
    
    resultsDiv.innerHTML = players.map(player => {
        console.log('Player object:', player); // Debug each player
        console.log('Player.ID:', player.ID, 'Player.id:', player.id, 'Player keys:', Object.keys(player));
        
        // Try both ID and id (case variations)
        const playerId = player.ID || player.id || player.athleteId;
        
        return `
            <div class="result-item" data-player-id="${playerId}" data-player-name="${player.name}">
                <strong>${player.name}</strong>
                ${player.position ? `<span class="position">${player.position}</span>` : ''}
            </div>
        `;
    }).join('');
    
    resultsDiv.classList.add('active');
}

// Select player
function selectPlayer(id, name, playerKey) {
    // Convert id to number
    const playerId = parseInt(id, 10);
    
    if (playerKey === 'A') {
        selectedPlayerA = { id: playerId, name };
        selectedA.innerHTML = `
            <div class="selected-info">
                <span class="player-name">${name}</span>
                <button class="remove-btn" onclick="clearPlayer('A')">✕</button>
            </div>
        `;
        searchInputA.value = '';
        resultsA.innerHTML = '';
        resultsA.classList.remove('active');
    } else {
        selectedPlayerB = { id: playerId, name };
        selectedB.innerHTML = `
            <div class="selected-info">
                <span class="player-name">${name}</span>
                <button class="remove-btn" onclick="clearPlayer('B')">✕</button>
            </div>
        `;
        searchInputB.value = '';
        resultsB.innerHTML = '';
        resultsB.classList.remove('active');
    }
    
    updateCompareButton();
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

// Update compare button state
function updateCompareButton() {
    compareBtn.disabled = !selectedPlayerA || !selectedPlayerB;
}

// Get selected stats type
function getSelectedStatsType() {
    const selected = document.querySelector('input[name="statsType"]:checked');
    return parseInt(selected.value);
}

// Compare players
async function comparePlayers() {
    if (!selectedPlayerA || !selectedPlayerB) return;

    const statsType = getSelectedStatsType();
    
    console.log('Comparing players:', {
        playerA: selectedPlayerA,
        playerB: selectedPlayerB,
        statsType: statsType
    });
    
    // Show loading
    loading.classList.remove('hidden');
    comparisonResults.classList.add('hidden');
    compareBtn.disabled = true;

    try {
        const requestBody = {
            aID: selectedPlayerA.id,
            bID: selectedPlayerB.id,
            type: statsType
        };
        
        console.log('Request body:', requestBody);
        
        const response = await fetch(`${API_BASE}/compare`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) throw new Error('Comparison failed');
        
        const result = await response.json();
        console.log('Comparison result:', result);
        displayComparisonResults(result);
    } catch (error) {
        console.error('Comparison error:', error);
        alert('Comparison failed. Please try again.');
    } finally {
        loading.classList.add('hidden');
        compareBtn.disabled = false;
    }
}

// Display comparison results
function displayComparisonResults(result) {
    // Update winner banner
    document.getElementById('overallWinner').textContent = result.overallWinner || 'Tie';

    // Update 1v1 prediction
    document.getElementById('oneVsOne').textContent = result.oneVsOnePrediction || 'N/A';

    // Update player strengths with names
    document.getElementById('player1NameStrengths').textContent = 
        `${selectedPlayerA.name} Strengths`;
    document.getElementById('player1Strengths').textContent = 
        result.player1Strengths || 'N/A';

    document.getElementById('player2NameStrengths').textContent = 
        `${selectedPlayerB.name} Strengths`;
    document.getElementById('player2Strengths').textContent = 
        result.player2Strengths || 'N/A';

    // Update conclusion
    document.getElementById('conclusion').textContent = result.conclusion || 'N/A';

    // Update full analysis
    document.getElementById('fullAnalysis').textContent = 
        result.analysis || 'No detailed analysis available';

    // Show results
    comparisonResults.classList.remove('hidden');
    comparisonResults.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
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
        const id = item.dataset.playerId;
        const name = item.dataset.playerName;
        console.log('Selected Player A - ID:', id, 'Name:', name, 'Item:', item);
        selectPlayer(id, name, 'A');
    }
});

resultsB.addEventListener('click', (e) => {
    const item = e.target.closest('.result-item');
    if (item) {
        const id = item.dataset.playerId;
        const name = item.dataset.playerName;
        console.log('Selected Player B - ID:', id, 'Name:', name, 'Item:', item);
        selectPlayer(id, name, 'B');
    }
});

// Compare button
compareBtn.addEventListener('click', comparePlayers);

// Close search results when clicking outside
document.addEventListener('click', (e) => {
    if (!e.target.closest('.player-box')) {
        resultsA.classList.remove('active');
        resultsB.classList.remove('active');
    }
});

// Make clearPlayer function global
window.clearPlayer = clearPlayer;