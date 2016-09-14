import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugins.codecs.HTMLCodec
import org.grails.taglib.encoder.OutputEncodingStack
import spock.lang.Specification

@TestFor(NavigationTagLib)
@TestMixin(GrailsUnitTestMixin)
class NavigationTagLibSpec extends Specification {

    void setup() {
       mockCodec(HTMLCodec)
    }

    void "testEachItemByController"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs': [
                    [controller:'dummy', action:'index', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', path:['dummy', 'get']]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        tagLib.eachItem([controller:'dummy', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}&"
        })

        OutputEncodingStack.OutputProxyWriter output = tagLib.out
        String outString = ""
        output.write(outString)

        def outcome = outString.split('&')

        then:
        'Action:index|Active:false' == outcome[0]
        'Action:get|Active:false' == outcome[1]
    }

    void "testEachItemActiveByPathDeepHit"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs': [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']] ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')

        then:
        'Action:index|Active:false|Title:Dummy' == outcome[0]
        'Action:get|Active:true|Title:Get' == outcome[1]
    }


    void "testEachItemActiveByPathSubItemHit"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs': [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']] ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')

        then:
        'Action:index|Active:false|Title:Dummy' == outcome[0]
        'Action:get|Active:true|Title:Get' == outcome[1]
    }

    void "testEachSubItemActiveByPathDeepHit"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs': [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']]]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachSubItem([activePath:'something/else/here/searching', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')

        then:
        'Action:search|Active:true|Title:null' == outcome[0]
    }

    void "testEachSubItemNotActiveByPathDeepHit"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs': [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['something', 'else', 'here'],
                        subItems:[ [action:'search', path:['something', 'else', 'here', 'searching']]]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'something'
        tagLib.metaClass.actionName = 'something'
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.eachSubItem([activePath:'something/else/here', group:'tabs'], {
            "Action:${it.action}|Active:${it.active}|Title:${it.title}&"
        })
        def outcome = tagLib.out.toString().split('&')

        then:
        'Action:search|Active:false|Title:null' == outcome[0]
    }

    void "testRenderSubItems"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs':
                [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['dummy', 'get'],
                        subItems:[
                            [action:'search', path:['dummy', 'search']],
                            [action:'test', path:['dummy', 'test']]
                        ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'dummy'
        tagLib.metaClass.actionName = 'get'
        tagLib.metaClass.message = { args -> args.code }
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.renderSubItems([group:'tabs'])
        def outcome = tagLib.out.toString()

        println outcome

        then:
        outcome.contains('search')
        outcome.contains('test')
    }

    void "testRenderSubItemsPathInSubItem"() {
        when:
        tagLib.navigationService = [
            byGroup: ['tabs':
                [
                    [controller:'dummy', action:'index', title:'Dummy', path:['dummy', 'index']],
                    [controller:'dummy', action:'get', title:'Get', path:['dummy', 'get'],
                        subItems:[
                                [action:'search', path:['dummy', 'search']],
                                [action:'test', path:['dummy', 'test']]
                        ]
                    ]
                ]
            ],
            reverseMapActivePathFor: { con, act, params -> [con, act]}
        ]

        tagLib.metaClass.controllerName = 'dummy'
        tagLib.metaClass.actionName = 'test'
        tagLib.metaClass.message = { args -> args.code }
        tagLib.metaClass.createLink = { args -> "link" }

        def first = true
        tagLib.renderSubItems([group:'tabs'])
        def outcome = tagLib.out.toString()

        println outcome

        then:
        outcome.contains('search')
        outcome.contains('test')
    }

    void "testDoPathsIntersect"() {
        when:
        def cases = [
            [a:['dummy', 'test'], b:['dummy'], result: true],
            [a:['dummy', 'test'], b:['dummy', 'search'], result: true],
            [a:['dummy'],         b:['dummy', 'search'], result: true],
            [a:['something'],     b:['dummy', 'search'], result: false]
        ]

        then:
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.doPathsIntersect(e.a, e.b)
        }
    }

    void "testPathShouldBeActive"() {
        when:
        def cases = [
            [itemPath:['dummy'],            currentPath:['dummy', 'x'],      result: true],
            [itemPath:['dummy', 'x'],       currentPath:['dummy', 'x'],      result: true],
            [itemPath:['dummy', 'x', 'y'],  currentPath:['dummy', 'x'],      result: false],
            [itemPath:['dummy', 'test'],    currentPath:['dummy'],           result: false],
            [itemPath:['dummy', 'test'],    currentPath:['dummy', 'x'],      result: false],
            [itemPath:['dummy'],            currentPath:['dummy'],           result: true],
            [itemPath:[],                   currentPath:['dummy'],           result: false],
            [itemPath:['something'],        currentPath:['dummy'],           result: false]
        ]

        then:
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.pathIsActive(e.itemPath, e.currentPath)
        }
    }

    void "testIsPathFullyEncapsulatedBy"() {
        when:
        def cases = [
            [a:['dummy', 'test'], b:['dummy'], result: false],
            [a:['dummy', 'test'], b:['dummy', 'search'], result: false],
            [a:['dummy'],         b:['dummy', 'search'], result: true],
            [a:['something'],     b:['dummy', 'search'], result: false]
        ]

        then:
        cases.each { e ->
            assertEquals "Case ${e} failed", e.result, tagLib.isPathFullyEncapsulatedBy(e.a, e.b)
        }
    }
}
