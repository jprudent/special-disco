# Special Disco

Where I play with live journals

Result available at [https://jprudent.github.io/special-disco/]()

## Dev

In REPL
```
(require '[nextjournal.clerk :as clerk])
(clerk/serve! {:browse true :watch-paths ["daily_coding_problem"]})
```

Live code with [http://localhost:7777/]() open

## How to publish a journal ?

Modify deps.edn and add your journal file in it : 

```clj
:paths ["daily_coding_problem/day_0001.clj"
        ".... blip ... blop ..."
        "daily_coding_problem/day_9999.clj"]
```

(maybe there is some support for file globbing ?)

Just merge on `public-release` branch. Github actions will do the rest.