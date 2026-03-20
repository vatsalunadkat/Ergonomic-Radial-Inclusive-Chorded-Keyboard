package com.vatoo.erick.shared

/**
 * Trie-based word prediction engine that provides:
 * 1. Word completions based on a typed prefix
 * 2. Spelling corrections for common typos (edit distance)
 * 3. Frequency-weighted suggestions
 */
class WordPredictionEngine {

    private val root = TrieNode()
    private var wordCount = 0

    // ── Trie data structure ──

    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isWord = false
        var frequency = 0
    }

    fun insert(word: String, frequency: Int = 1) {
        if (word.isBlank()) return
        val lower = word.lowercase().trim()
        var node = root
        for (ch in lower) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        if (!node.isWord) wordCount++
        node.isWord = true
        node.frequency = maxOf(node.frequency, frequency)
    }

    fun contains(word: String): Boolean {
        val lower = word.lowercase().trim()
        var node = root
        for (ch in lower) {
            node = node.children[ch] ?: return false
        }
        return node.isWord
    }

    // ── Word completion ──

    /**
     * Returns up to [limit] word completions for the given [prefix],
     * sorted by frequency (descending).
     */
    fun getCompletions(prefix: String, limit: Int = 3): List<String> {
        if (prefix.isBlank()) return emptyList()
        val lower = prefix.lowercase().trim()

        // Navigate to prefix node
        var node = root
        for (ch in lower) {
            node = node.children[ch] ?: return emptyList()
        }

        // Collect all words under this prefix
        val results = mutableListOf<Pair<String, Int>>() // word -> frequency
        collectWords(node, StringBuilder(lower), results, limit * 4) // collect extra for sorting

        return results
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    private fun collectWords(
        node: TrieNode,
        current: StringBuilder,
        results: MutableList<Pair<String, Int>>,
        maxCollect: Int
    ) {
        if (results.size >= maxCollect) return
        if (node.isWord) {
            results.add(current.toString() to node.frequency)
        }
        // Visit children in alphabetical order for deterministic results
        for ((ch, child) in node.children.entries.sortedBy { it.key }) {
            current.append(ch)
            collectWords(child, current, results, maxCollect)
            current.deleteAt(current.length - 1)
            if (results.size >= maxCollect) return
        }
    }

    // ── Autocorrect / spelling correction ──

    /**
     * Returns up to [limit] spelling corrections for [word] using
     * Levenshtein edit distance (max distance = [maxDistance]).
     * Only returns corrections that are different from the input word.
     */
    fun getCorrections(word: String, limit: Int = 3, maxDistance: Int = 2): List<String> {
        if (word.isBlank()) return emptyList()
        val lower = word.lowercase().trim()

        // If the word is already correct, no corrections needed
        if (contains(lower)) return emptyList()

        val candidates = mutableListOf<Pair<String, Int>>() // word -> frequency
        collectCorrectionCandidates(root, StringBuilder(), lower, maxDistance, candidates)

        return candidates
            .sortedWith(compareByDescending { it.second })
            .map { it.first }
            .filter { it != lower }
            .take(limit)
    }

    private fun collectCorrectionCandidates(
        node: TrieNode,
        current: StringBuilder,
        target: String,
        maxDistance: Int,
        results: MutableList<Pair<String, Int>>
    ) {
        if (node.isWord) {
            val dist = editDistance(current.toString(), target)
            if (dist <= maxDistance) {
                results.add(current.toString() to node.frequency)
            }
        }

        // Prune: if the current path is already too far from the target, stop
        val minPossibleDist = minEditDistanceBound(current.toString(), target)
        if (minPossibleDist > maxDistance) return

        for ((ch, child) in node.children) {
            current.append(ch)
            collectCorrectionCandidates(child, current, target, maxDistance, results)
            current.deleteAt(current.length - 1)
        }
    }

    // ── Suggestion API (unified completions + corrections) ──

    /**
     * Returns up to [limit] suggestions for the current word.
     * Prioritizes exact-prefix completions, then fills with corrections.
     */
    fun getSuggestions(currentWord: String, limit: Int = 3): List<String> {
        if (currentWord.isBlank()) return emptyList()
        val lower = currentWord.lowercase().trim()

        val completions = getCompletions(lower, limit)
        if (completions.size >= limit) return completions

        // Fill remaining slots with corrections
        val completionSet = completions.toSet()
        val corrections = getCorrections(lower, limit, maxDistance = 1)
            .filter { it !in completionSet && it != lower }

        return (completions + corrections).take(limit)
    }

    // ── Edit distance utilities ──

    private fun editDistance(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        // Use single-row DP for space efficiency
        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,      // deletion
                    curr[j - 1] + 1,  // insertion
                    prev[j - 1] + cost // substitution
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }

    /**
     * Lower bound on edit distance between a partial prefix and
     * any completion of that prefix vs the target.
     * Used for pruning the trie search.
     */
    private fun minEditDistanceBound(prefix: String, target: String): Int {
        val m = prefix.length
        val n = target.length

        // If prefix is longer than target + maxDistance, prune
        if (m > n + 2) return m - n

        // Compute edit distance between prefix and target[0..m] truncated
        val truncTarget = if (m <= n) target.substring(0, m) else target
        return editDistance(prefix, truncTarget)
    }

    // ── Built-in dictionary ──

    companion object {
        /**
         * Creates a WordPredictionEngine pre-loaded with a common English dictionary.
         * Words are assigned frequency tiers so common words rank higher.
         */
        fun createWithDefaultDictionary(): WordPredictionEngine {
            val engine = WordPredictionEngine()
            loadDefaultDictionary(engine)
            return engine
        }

        private fun loadDefaultDictionary(engine: WordPredictionEngine) {
            // Tier 1: Ultra-common words (freq 1000)
            val tier1 = listOf(
                "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
                "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
                "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
                "or", "an", "will", "my", "one", "all", "would", "there", "their",
                "what", "so", "up", "out", "if", "about", "who", "get", "which", "go",
                "me", "when", "make", "can", "like", "time", "no", "just", "him",
                "know", "take", "people", "into", "year", "your", "good", "some",
                "could", "them", "see", "other", "than", "then", "now", "look",
                "only", "come", "its", "over", "think", "also", "back", "after",
                "use", "two", "how", "our", "work", "first", "well", "way", "even",
                "new", "want", "because", "any", "these", "give", "day", "most", "us",
                "is", "are", "was", "were", "been", "has", "had", "did", "does",
                "am", "being", "having", "doing", "going", "got", "getting",
                "very", "much", "more", "many", "too", "here", "where"
            )

            // Tier 2: Common words (freq 500)
            val tier2 = listOf(
                "great", "help", "through", "long", "right", "own", "still", "find",
                "while", "last", "might", "before", "old", "never", "world", "life",
                "same", "another", "should", "home", "big", "tell", "end", "does",
                "move", "try", "kind", "hand", "call", "keep", "turn", "between",
                "need", "every", "each", "made", "really", "already", "start",
                "always", "place", "off", "under", "three", "began", "show",
                "house", "both", "number", "part", "name", "being", "read", "run",
                "small", "set", "put", "always", "high", "live", "left", "down",
                "ask", "may", "change", "around", "little", "head", "thought",
                "still", "found", "few", "side", "next", "without", "stop",
                "open", "close", "best", "better", "ever", "enough", "line", "hear",
                "water", "food", "money", "group", "point", "city", "sure", "real",
                "word", "quite", "soon", "done", "family", "love", "school",
                "children", "country", "night", "away", "again", "room", "body",
                "morning", "friend", "book", "problem", "nothing", "face", "door",
                "play", "feel", "together", "until", "hard", "power", "story",
                "today", "important", "young", "different", "second", "woman",
                "early", "white", "girl", "man", "boy", "game", "eyes", "case",
                "state", "company", "service"
            )

            // Tier 3: Everyday words (freq 200)
            val tier3 = listOf(
                "happy", "beautiful", "believe", "please", "thank", "sorry",
                "hello", "goodbye", "tomorrow", "yesterday", "tonight",
                "together", "today", "always", "sometimes", "often", "usually",
                "never", "maybe", "probably", "possible", "actually", "already",
                "information", "business", "government", "question", "answer",
                "experience", "example", "because", "different", "important",
                "understand", "remember", "interest", "complete", "continue",
                "country", "between", "through", "against", "however", "although",
                "address", "animal", "weather", "music", "color", "picture",
                "computer", "phone", "email", "message", "number", "program",
                "system", "market", "health", "education", "language",
                "kitchen", "window", "garden", "office", "meeting", "project",
                "develop", "create", "build", "design", "write", "paper",
                "report", "heart", "light", "table", "watch", "walk",
                "travel", "smile", "laugh", "drive", "sleep", "dream",
                "eat", "drink", "break", "think", "learn", "teach",
                "speak", "listen", "follow", "happen", "include",
                "provide", "consider", "appear", "allow", "discuss",
                "receive", "expect", "suggest", "produce", "offer",
                "support", "require", "prepare", "accept", "decision",
                "simple", "strong", "clear", "certain", "special",
                "reason", "minute", "moment", "person", "result"
            )

            // Tier 4: Extended vocabulary (freq 100)
            val tier4 = listOf(
                "about", "above", "across", "after", "afternoon", "again",
                "almost", "along", "anything", "apply", "area",
                "attention", "available", "ball", "bank", "basis", "become",
                "before", "behind", "below", "beside", "blood", "blue",
                "board", "bottom", "bring", "brother", "building", "buy",
                "camera", "carry", "catch", "center", "chair",
                "character", "check", "church", "class", "clean", "clothes",
                "collect", "college", "common", "compare", "concern",
                "condition", "connect", "control", "corner", "cost",
                "cover", "cross", "current", "cut", "daily", "dance",
                "dark", "daughter", "dead", "deal", "deep", "degree",
                "describe", "detail", "determine", "dinner", "direct",
                "discover", "distance", "doctor", "draw", "dress",
                "drop", "during", "edge", "effect", "effort", "either",
                "energy", "enjoy", "enter", "environment", "equal",
                "especially", "evening", "event", "exactly", "explain",
                "express", "extra", "eye", "fail", "fall", "fast",
                "father", "fear", "figure", "fill", "final", "finger",
                "finish", "fire", "floor", "fly", "foot", "force",
                "foreign", "forget", "form", "forward", "four", "free",
                "front", "full", "fun", "future", "general", "glass",
                "goal", "gold", "green", "ground", "grow", "guess",
                "gun", "hair", "half", "hang", "happen", "hour",
                "husband", "idea", "imagine", "industry", "inside",
                "instead", "issue", "job", "join", "jump", "king",
                "knowledge", "land", "late", "law", "lay", "lead",
                "letter", "level", "lie", "list", "lose", "lot",
                "low", "machine", "main", "major", "matter", "mean",
                "measure", "media", "medical", "member", "memory",
                "mention", "middle", "might", "military", "million",
                "mind", "miss", "model", "modern", "month", "mother",
                "mountain", "mouth", "myself", "nation", "natural",
                "near", "necessary", "news", "nice", "north", "note",
                "notice", "oil", "once", "opportunity", "order",
                "outside", "page", "paint", "parent", "particular",
                "pass", "past", "patient", "pay", "peace", "perform",
                "period", "pick", "piece", "plan", "plant", "player",
                "please", "policy", "political", "poor", "popular",
                "position", "positive", "pressure", "pretty", "price",
                "private", "process", "professional", "protect", "prove",
                "public", "pull", "push", "raise", "range", "rate",
                "reach", "record", "red", "reduce", "region",
                "relate", "remain", "remove", "repeat", "replace",
                "represent", "research", "resource", "rest", "return",
                "reveal", "rich", "rise", "risk", "road", "rock",
                "role", "rule", "safe", "save", "scene", "season",
                "seat", "sell", "send", "senior", "sense", "serious",
                "serve", "seven", "share", "short", "shot", "sign",
                "significant", "similar", "since", "single", "sister",
                "sit", "situation", "six", "size", "skill", "social",
                "society", "soldier", "somebody", "someone", "song",
                "sort", "sound", "south", "space", "spend", "sport",
                "spring", "staff", "stage", "stand", "standard", "star",
                "stock", "store", "street", "student", "study", "style",
                "subject", "success", "summer", "surface", "table",
                "technology", "television", "ten", "test", "themselves",
                "theory", "thing", "thousand", "threat", "throw",
                "tie", "top", "total", "tough", "toward", "trade",
                "traditional", "training", "treat", "tree", "trial",
                "trip", "trouble", "true", "truth", "type", "unit",
                "value", "various", "view", "visit", "voice", "vote",
                "wait", "wall", "want", "war", "west", "whatever",
                "whole", "wide", "wife", "win", "wish", "wonder",
                "worker", "wrong", "yard", "yeah", "young"
            )

            for (w in tier1) engine.insert(w, tier1.size + 1 - tier1.indexOf(w) + 900)
            for (w in tier2) engine.insert(w, 500)
            for (w in tier3) engine.insert(w, 200)
            for (w in tier4) engine.insert(w, 100)
        }
    }
}
