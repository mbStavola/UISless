UISless
=======

Workaround for the awful service that is UIS... that we never finished.

How it works
=======
1. User provides their UIS info via a configuration json (located in resources)
2. UISless logs them into UIS via HtmlUnit
3. Courses are scraped and inserted into a DB

TODO
=======
- I took out the navigation to subpages when I was rewriting this, but if you want room info you need to do that
- This is kinda tailored to Heroku, maybe make it more platform agnostic?
- UISless is really only a course scraper right now, but the original vision was much more than that!
- There might be some problems to iron out in general :)
- git gud --hard